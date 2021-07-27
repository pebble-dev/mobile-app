import 'dart:io';
import 'dart:typed_data';

import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final ConnectionControl connectionControl = ConnectionControl();

class DevConnection extends StateNotifier<DevConnState> {
  HttpServer? _server;
  bool _isConnected = false;

  DevConnection() : super(DevConnState(false, false));

  void close() {
    _server?.close();
    _server = null;
    _isConnected = false;

    _updateState();
  }

  void handleDevConnection(WebSocket socket, String ip) {
    if (!_isConnected) {
      _isConnected = true;
      _updateState();

      socket.listen((event) {
        Uint8List indata = event as Uint8List;
        if (indata[0] == 0x01) {
          Uint8List packet = indata.sublist(1);
          ListWrapper packetDataWrapper = ListWrapper();
          packetDataWrapper.value = packet.map((e) => e.toInt()).toList();
          connectionControl.sendRawPacket(packetDataWrapper);
          /*.then((res) {
            Uint8List rpacket = Uint8List(0x00) + (res as Uint8List);
            socket.add(rpacket);
          });*/
        }
      }, onDone: () {
        _isConnected = false;
        _updateState();
      }, onError: (error) {
        _isConnected = false;
        _updateState();
        print("Dev connection error: error");
      }, cancelOnError: true);
    } else {
      socket.close(WebSocketStatus.internalServerError,
          "Only one developer connection is supported");
    }
  }

  Future<void> start() async {
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
    print('Update state ${_server} ${_isConnected}');
    state = DevConnState(_server != null, _isConnected);
  }
}

class DevConnState {
  final bool running;
  final bool connected;

  DevConnState(this.running, this.connected);
}

final devConnectionProvider = StateNotifierProvider<DevConnection>((ref) {
  return DevConnection();
});
