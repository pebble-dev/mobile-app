import 'dart:async';
import 'dart:isolate';
import 'dart:ui';

import 'package:cobble/infrastructure/backgroundcomm/RpcRequest.dart';
import 'package:cobble/infrastructure/backgroundcomm/RpcResult.dart';

import 'BackgroundRpc.dart';

typedef ReceivingFunction = Future<Object> Function(Object input);

void startReceivingRpcRequests(ReceivingFunction receivingFunction) {
  final receivingPort = ReceivePort();
  IsolateNameServer.removePortNameMapping(isolatePortNameToBackground);
  IsolateNameServer.registerPortWithName(
    receivingPort.sendPort,
    isolatePortNameToBackground,
  );

  receivingPort.listen((message) {
    Future.microtask(() async {
      if (message is! RpcRequest) {
        throw Exception("Message is not RpcRequest: $message");
      }

      final request = message as RpcRequest;

      RpcResult result;
      try {
        final resultObject = await receivingFunction(request.input);
        result = RpcResult.success(request.requestId, resultObject);
      } catch (e, stackTrace) {
        print(e);
        result = RpcResult.error(request.requestId, e, stackTrace);
      }

      final returnPort = IsolateNameServer.lookupPortByName(
        isolatePortNameReturnFromBackground,
      );

      if (returnPort != null) {
        returnPort.send(result);
      }

      // If returnPort is null, then receiver died and
      // does not care about the result anymore. Just drop it.
    });
  });
}
