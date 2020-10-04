// Autogenerated from Pigeon (v0.1.6), do not edit directly.
// See also: https://pub.dev/packages/pigeon
// ignore_for_file: public_member_api_docs, non_constant_identifier_names, avoid_as, unused_import
import 'dart:async';
import 'package:flutter/services.dart';
import 'dart:typed_data' show Uint8List, Int32List, Int64List, Float64List;

class ListWrapper {
  List value;
  // ignore: unused_element
  Map<dynamic, dynamic> _toMap() {
    final Map<dynamic, dynamic> pigeonMap = <dynamic, dynamic>{};
    pigeonMap['value'] = value;
    return pigeonMap;
  }
  // ignore: unused_element
  static ListWrapper _fromMap(Map<dynamic, dynamic> pigeonMap) {
    if (pigeonMap == null){
      return null;
    }
    final ListWrapper result = ListWrapper();
    result.value = pigeonMap['value'];
    return result;
  }
}

class BooleanWrapper {
  bool value;
  // ignore: unused_element
  Map<dynamic, dynamic> _toMap() {
    final Map<dynamic, dynamic> pigeonMap = <dynamic, dynamic>{};
    pigeonMap['value'] = value;
    return pigeonMap;
  }
  // ignore: unused_element
  static BooleanWrapper _fromMap(Map<dynamic, dynamic> pigeonMap) {
    if (pigeonMap == null){
      return null;
    }
    final BooleanWrapper result = BooleanWrapper();
    result.value = pigeonMap['value'];
    return result;
  }
}

class WatchConnectionState {
  bool isConnected;
  bool isConnecting;
  int currentWatchAddress;
  // ignore: unused_element
  Map<dynamic, dynamic> _toMap() {
    final Map<dynamic, dynamic> pigeonMap = <dynamic, dynamic>{};
    pigeonMap['isConnected'] = isConnected;
    pigeonMap['isConnecting'] = isConnecting;
    pigeonMap['currentWatchAddress'] = currentWatchAddress;
    return pigeonMap;
  }
  // ignore: unused_element
  static WatchConnectionState _fromMap(Map<dynamic, dynamic> pigeonMap) {
    if (pigeonMap == null){
      return null;
    }
    final WatchConnectionState result = WatchConnectionState();
    result.isConnected = pigeonMap['isConnected'];
    result.isConnecting = pigeonMap['isConnecting'];
    result.currentWatchAddress = pigeonMap['currentWatchAddress'];
    return result;
  }
}

class NumberWrapper {
  int value;
  // ignore: unused_element
  Map<dynamic, dynamic> _toMap() {
    final Map<dynamic, dynamic> pigeonMap = <dynamic, dynamic>{};
    pigeonMap['value'] = value;
    return pigeonMap;
  }
  // ignore: unused_element
  static NumberWrapper _fromMap(Map<dynamic, dynamic> pigeonMap) {
    if (pigeonMap == null){
      return null;
    }
    final NumberWrapper result = NumberWrapper();
    result.value = pigeonMap['value'];
    return result;
  }
}

class NotificationsControl {
  Future<void> sendTestNotification() async {
    const BasicMessageChannel<dynamic> channel =
        BasicMessageChannel<dynamic>('dev.flutter.pigeon.NotificationsControl.sendTestNotification', StandardMessageCodec());
    
    final Map<dynamic, dynamic> replyMap = await channel.send(null);
    if (replyMap == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
        details: null);
    } else if (replyMap['error'] != null) {
      final Map<dynamic, dynamic> error = replyMap['error'];
      throw PlatformException(
          code: error['code'],
          message: error['message'],
          details: error['details']);
    } else {
      // noop
    }
    
  }
}

class ScanControl {
  Future<void> startBleScan() async {
    const BasicMessageChannel<dynamic> channel =
        BasicMessageChannel<dynamic>('dev.flutter.pigeon.ScanControl.startBleScan', StandardMessageCodec());
    
    final Map<dynamic, dynamic> replyMap = await channel.send(null);
    if (replyMap == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
        details: null);
    } else if (replyMap['error'] != null) {
      final Map<dynamic, dynamic> error = replyMap['error'];
      throw PlatformException(
          code: error['code'],
          message: error['message'],
          details: error['details']);
    } else {
      // noop
    }
    
  }
  Future<void> startClassicScan() async {
    const BasicMessageChannel<dynamic> channel =
        BasicMessageChannel<dynamic>('dev.flutter.pigeon.ScanControl.startClassicScan', StandardMessageCodec());
    
    final Map<dynamic, dynamic> replyMap = await channel.send(null);
    if (replyMap == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
        details: null);
    } else if (replyMap['error'] != null) {
      final Map<dynamic, dynamic> error = replyMap['error'];
      throw PlatformException(
          code: error['code'],
          message: error['message'],
          details: error['details']);
    } else {
      // noop
    }
    
  }
}

abstract class ScanCallbacks {
  void onScanUpdate(ListWrapper arg);
  void onScanStarted();
  void onScanStopped();
  static void setup(ScanCallbacks api) {
    {
      const BasicMessageChannel<dynamic> channel =
          BasicMessageChannel<dynamic>('dev.flutter.pigeon.ScanCallbacks.onScanUpdate', StandardMessageCodec());
      channel.setMessageHandler((dynamic message) async {
        final Map<dynamic, dynamic> mapMessage = message as Map<dynamic, dynamic>;
        final ListWrapper input = ListWrapper._fromMap(mapMessage);
        api.onScanUpdate(input);
      });
    }
    {
      const BasicMessageChannel<dynamic> channel =
          BasicMessageChannel<dynamic>('dev.flutter.pigeon.ScanCallbacks.onScanStarted', StandardMessageCodec());
      channel.setMessageHandler((dynamic message) async {
        api.onScanStarted();
      });
    }
    {
      const BasicMessageChannel<dynamic> channel =
          BasicMessageChannel<dynamic>('dev.flutter.pigeon.ScanCallbacks.onScanStopped', StandardMessageCodec());
      channel.setMessageHandler((dynamic message) async {
        api.onScanStopped();
      });
    }
  }
}

class AppLifecycleControl {
  Future<BooleanWrapper> waitForBoot() async {
    const BasicMessageChannel<dynamic> channel =
        BasicMessageChannel<dynamic>('dev.flutter.pigeon.AppLifecycleControl.waitForBoot', StandardMessageCodec());
    
    final Map<dynamic, dynamic> replyMap = await channel.send(null);
    if (replyMap == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
        details: null);
    } else if (replyMap['error'] != null) {
      final Map<dynamic, dynamic> error = replyMap['error'];
      throw PlatformException(
          code: error['code'],
          message: error['message'],
          details: error['details']);
    } else {
      return BooleanWrapper._fromMap(replyMap['result']);
    }
    
  }
}

abstract class ConnectionCallbacks {
  void onWatchConnectionStateChanged(WatchConnectionState arg);
  static void setup(ConnectionCallbacks api) {
    {
      const BasicMessageChannel<dynamic> channel =
          BasicMessageChannel<dynamic>('dev.flutter.pigeon.ConnectionCallbacks.onWatchConnectionStateChanged', StandardMessageCodec());
      channel.setMessageHandler((dynamic message) async {
        final Map<dynamic, dynamic> mapMessage = message as Map<dynamic, dynamic>;
        final WatchConnectionState input = WatchConnectionState._fromMap(mapMessage);
        api.onWatchConnectionStateChanged(input);
      });
    }
  }
}

class DebugControl {
  Future<void> collectLogs() async {
    const BasicMessageChannel<dynamic> channel =
        BasicMessageChannel<dynamic>('dev.flutter.pigeon.DebugControl.collectLogs', StandardMessageCodec());
    
    final Map<dynamic, dynamic> replyMap = await channel.send(null);
    if (replyMap == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
        details: null);
    } else if (replyMap['error'] != null) {
      final Map<dynamic, dynamic> error = replyMap['error'];
      throw PlatformException(
          code: error['code'],
          message: error['message'],
          details: error['details']);
    } else {
      // noop
    }
    
  }
}

class ConnectionControl {
  Future<BooleanWrapper> isConnected() async {
    const BasicMessageChannel<dynamic> channel =
        BasicMessageChannel<dynamic>('dev.flutter.pigeon.ConnectionControl.isConnected', StandardMessageCodec());
    
    final Map<dynamic, dynamic> replyMap = await channel.send(null);
    if (replyMap == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
        details: null);
    } else if (replyMap['error'] != null) {
      final Map<dynamic, dynamic> error = replyMap['error'];
      throw PlatformException(
          code: error['code'],
          message: error['message'],
          details: error['details']);
    } else {
      return BooleanWrapper._fromMap(replyMap['result']);
    }
    
  }
  Future<void> connectToWatch(NumberWrapper arg) async {
    final Map<dynamic, dynamic> requestMap = arg._toMap();
    const BasicMessageChannel<dynamic> channel =
        BasicMessageChannel<dynamic>('dev.flutter.pigeon.ConnectionControl.connectToWatch', StandardMessageCodec());
    
    final Map<dynamic, dynamic> replyMap = await channel.send(requestMap);
    if (replyMap == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
        details: null);
    } else if (replyMap['error'] != null) {
      final Map<dynamic, dynamic> error = replyMap['error'];
      throw PlatformException(
          code: error['code'],
          message: error['message'],
          details: error['details']);
    } else {
      // noop
    }
    
  }
  Future<void> sendRawPacket(ListWrapper arg) async {
    final Map<dynamic, dynamic> requestMap = arg._toMap();
    const BasicMessageChannel<dynamic> channel =
        BasicMessageChannel<dynamic>('dev.flutter.pigeon.ConnectionControl.sendRawPacket', StandardMessageCodec());
    
    final Map<dynamic, dynamic> replyMap = await channel.send(requestMap);
    if (replyMap == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
        details: null);
    } else if (replyMap['error'] != null) {
      final Map<dynamic, dynamic> error = replyMap['error'];
      throw PlatformException(
          code: error['code'],
          message: error['message'],
          details: error['details']);
    } else {
      // noop
    }
    
  }
}

