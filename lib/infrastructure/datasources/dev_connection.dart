import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/domain/connection/raw_incoming_packets_provider.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:network_info_plus/network_info_plus.dart';
import 'package:path_provider/path_provider.dart';

final ConnectionControl connectionControl = ConnectionControl();

class DevConnection extends StateNotifier<DevConnState> {
  HttpServer? _server;
  WebSocket? _connectedSocket;
  StreamSubscription<Uint8List>? _incomingWatchPacketsSubscription;
  final AppManager _appManager;

  final Stream<Uint8List> _pebbleIncomingPacketStream;

  String _localIp = "";

  DevConnection(this._pebbleIncomingPacketStream, this._appManager)
      : super(DevConnState(false, false, ""));

  void close() {
    disconnect();

    _server?.close();
    _server = null;

    _updateState();
  }

  void disconnect() {
    _connectedSocket?.close();
    _connectedSocket = null;

    _incomingWatchPacketsSubscription?.cancel();
    _incomingWatchPacketsSubscription = null;
  }

  void handleDevConnection(WebSocket socket, String ip) {
    if (_connectedSocket == null) {
      _connectedSocket = socket;
      _updateState();

      _incomingWatchPacketsSubscription =
          _pebbleIncomingPacketStream.listen(onPacketReceivedFromWatch);

      socket.listen(
        (event) {
          onPacketReceivedFromWebsocket(socket, event as Uint8List);
        },
        onDone: () {
          disconnect();
          _updateState();
        },
        onError: (error) {
          disconnect();
          _updateState();
        },
        cancelOnError: true,
      );
    } else {
      socket.close(WebSocketStatus.internalServerError,
          "Only one developer connection is supported");
    }
  }

  void onPacketReceivedFromWebsocket(WebSocket socket, Uint8List indata) {
    if (indata[0] == _packetFromWebsocketRelayToWatch) {
      Uint8List packet = indata.sublist(1);
      ListWrapper packetDataWrapper = ListWrapper();
      packetDataWrapper.value = packet.map((e) => e.toInt()).toList();
      connectionControl.sendRawPacket(packetDataWrapper);
      /*.then((res) {
        Uint8List rpacket = Uint8List(0x00) + (res as Uint8List);
        socket.add(rpacket);
      });*/
    } else if (indata[0] == _packetFromWebsocketInstallBundle) {
      Uint8List pbw = indata.sublist(1);
      _beginAppInstall(pbw, socket);
    }
  }

  void onPacketReceivedFromWatch(Uint8List data) {
    final connectedSocket = _connectedSocket;
    if (connectedSocket == null) {
      return;
    }

    final builder = BytesBuilder();
    builder.addByte(0x00);
    builder.add(data);

    var bytes = builder.toBytes();
    connectedSocket.add(bytes);
  }

  void _beginAppInstall(Uint8List pbwData, WebSocket socket) async {
    final dir = await getTemporaryDirectory();
    final tempPbwFile = File("${dir.path}/tmp.pbw");

    await tempPbwFile.writeAsBytes(pbwData, flush: true);

    final uri = tempPbwFile.uri.toString();
    final success = await _appManager.getAppInfoAndBeginAppInstall(uri);

    int status;
    if (success) {
      status = 0x00;
    } else {
      status = 0x01;
    }

    final builder = BytesBuilder();
    builder.addByte(0x05);
    builder.add(Uint8List(4)..buffer.asByteData().setInt32(0, status));

    var bytes = builder.toBytes();
    socket.add(bytes);
  }

  Future<void> start() async {
    _localIp = await (NetworkInfo().getWifiIP()) ?? "";

    final server = await HttpServer.bind(
      InternetAddress.anyIPv4,
      9000,
      shared: true,
    );

    _server = server;

    server.listen((event) {
      if (WebSocketTransformer.isUpgradeRequest(event)) {
        WebSocketTransformer.upgrade(event).then(
            (value) => handleDevConnection(value, server.address.address));
      }
    });

    _updateState();
  }

  void _updateState() {
    state = DevConnState(_server != null, _connectedSocket != null, _localIp);
  }
}

class DevConnState {
  final bool running;
  final bool connected;

  final String localIp;

  DevConnState(this.running, this.connected, this.localIp);
}

final devConnectionProvider = StateNotifierProvider<DevConnection>((ref) {
  final incomingPacketsStream = ref.read(rawPacketStreamProvider);
  final appManager = ref.read(appManagerProvider);
  return DevConnection(incomingPacketsStream, appManager);
});

final _packetFromWebsocketRelayToWatch = 0x01;
final _packetFromWebsocketInstallBundle = 0x04;
