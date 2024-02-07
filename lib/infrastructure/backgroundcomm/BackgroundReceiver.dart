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
      RpcRequest request;

      if (message is Map<String, dynamic>) {
        try {
          request = RpcRequest.fromMap(message);
        } catch (e) {
          throw Exception("Error creating RpcRequest from Map: $e");
        }
      } else {
        throw Exception("Message is not a Map representing RpcRequest: $message");
      }

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
        returnPort.send(result.toMap());
      }

      // If returnPort is null, then receiver died and
      // does not care about the result anymore. Just drop it.
    });
  });
}
