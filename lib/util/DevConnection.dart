import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class DevConnection {
  static final DevConnection _singleton = new DevConnection._internal();
  static final MethodChannel _packetIO = MethodChannel('io.rebble.fossil/packetIO');
  bool isConnected = false;
  HttpServer _server;
  Function _onServerStatChange; // (isrunning)
  Function _onConnChange; // (isconnected)

  factory DevConnection() {
    return _singleton;
  }

  DevConnection._internal() {
    //init
  }

  void setCB(Function onConnChange, Function onServerStatChange) {
    _onServerStatChange = onServerStatChange;
    _onConnChange = onConnChange;
  }

  void close() {
    _server.close();
    isConnected = false;
    _onServerStatChange(false);
  }

  void handleDevConnection(WebSocket socket, String ip) {
    if(!isConnected) {
      isConnected = true;
      _onConnChange(true);
      socket.listen((event) {
        Uint8List indata = event as Uint8List;
        if (indata[0] == 0x01) {
          Uint8List packet = indata.sublist(1);
          _packetIO.invokeMethod("send", packet);/*.then((res) {
            Uint8List rpacket = Uint8List(0x00) + (res as Uint8List);
            socket.add(rpacket);
          });*/
        }
      }, onDone: () {
        _onConnChange(false);
        isConnected = false;
      },
          onError: (error) {
            _onConnChange(false);
            print("Dev connection error: error");
            isConnected = false;
            }, cancelOnError: true);
    }else {
      socket.close(WebSocketStatus.internalServerError, "Only one developer connection is supported");
    }
  }

  Future<void> start() async {
    _server = await HttpServer.bind(InternetAddress.anyIPv4, 9000, shared: true);
    _onServerStatChange(true);
    _server.listen((event) {
      if(WebSocketTransformer.isUpgradeRequest(event)) {
        WebSocketTransformer.upgrade(event).then((value) => handleDevConnection(value, _server.address.address));
      }
    });
  }
}