import 'dart:async';
import 'dart:isolate';
import 'dart:ui';

import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/backgroundcomm/RpcRequest.dart';
import 'package:cobble/infrastructure/backgroundcomm/RpcResult.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

/// Helper class for triggering methods on the background flutter side.
///
/// System works by sending a specific object as an input. Then based on the
/// object you send, background will execute target method and return result.
///
/// This class never returns AsyncValue.loading (only data or error).
class BackgroundRpc {
  Map<int, Completer<AsyncValue<Object>>> _pendingCompleters = Map();
  int _nextMessageId = 0;

  BackgroundRpc() {
    _startReceivingResults();
  }

  Future<AsyncValue<O>> triggerMethod<I extends Object, O extends Object>(
    I input,
  ) async {
    final port = IsolateNameServer.lookupPortByName(
      isolatePortNameToBackground,
    );

    if (port == null) {
      throw Exception("Port not open. Is background running?");
    }

    final requestId = _nextMessageId++;

    final request = RpcRequest(requestId, input);

    final completer = Completer<AsyncValue<Object>>();
    _pendingCompleters[requestId] = completer;

    port.send(request.toMap());

    final result = await completer.future;
    return result as AsyncValue<O>;
  }

  void _startReceivingResults() {
    final returnPort = ReceivePort();
    IsolateNameServer.removePortNameMapping(
        isolatePortNameReturnFromBackground);
    IsolateNameServer.registerPortWithName(
        returnPort.sendPort, isolatePortNameReturnFromBackground);

    returnPort.listen((message) {
      RpcResult receivedMessage;

      if (message is Map<String, dynamic>) {
        try {
          receivedMessage = RpcResult.fromMap(message);
        } catch (e) {
          throw Exception("Error creating RpcResult from Map: $e");
        }
      } else {
        Log.e("Unknown message: $message");
        return;
      }

      final waitingCompleter = _pendingCompleters[receivedMessage.id];
      if (waitingCompleter == null) {
        return;
      }

      AsyncValue<Object> result;

      if (receivedMessage.successResult != null) {
        result = AsyncValue.data(receivedMessage.successResult!);
      } else if (receivedMessage.errorResult != null) {
        result = AsyncValue.error(
          receivedMessage.errorResult!,
          stackTrace: receivedMessage.errorStacktrace,
        );
      } else {
        result = AsyncValue.error("Received result without any data.");
      }

      waitingCompleter.complete(result);
    });
  }
}

final String isolatePortNameToBackground = "toBackground";
final String isolatePortNameReturnFromBackground = "returnFromBackground";

final backgroundRpcProvider = Provider<BackgroundRpc>((ref) => BackgroundRpc());
