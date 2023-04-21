import 'dart:async';
import 'dart:isolate';
import 'dart:ui';

import 'package:cobble/infrastructure/backgroundcomm/RpcRequest.dart';
import 'package:cobble/infrastructure/backgroundcomm/RpcResult.dart';

import 'BackgroundRpc.dart';

typedef ReceivingFunction = Future<Object> Function(String type, Object input);

void startReceivingRpcRequests(RpcDirection rpcDirection, ReceivingFunction receivingFunction) {
  final receivingPort = ReceivePort();
  IsolateNameServer.removePortNameMapping(
    rpcDirection == RpcDirection.toBackground
        ? isolatePortNameToBackground
        : isolatePortNameToForeground,
  );
  IsolateNameServer.registerPortWithName(
    receivingPort.sendPort,
    rpcDirection == RpcDirection.toBackground
        ? isolatePortNameToBackground
        : isolatePortNameToForeground,
  );

  receivingPort.listen((message) {
    Future.microtask(() async {
      message = RpcRequest.fromJson(message);
      if (message is! RpcRequest) {
        throw Exception("Message is not RpcRequest: $message");
      }

      final RpcRequest request = message;

      RpcResult result;
      try {
        final resultObject = await receivingFunction(request.type, request.input);
        result = RpcResult.success(request.requestId, resultObject);
      } catch (e, stackTrace) {
        print(e);
        result = RpcResult.error(request.requestId, e);
      }

      final returnPort = IsolateNameServer.lookupPortByName(
        rpcDirection == RpcDirection.toBackground
            ? isolatePortNameReturnFromBackground
            : isolatePortNameReturnFromForeground,
      );

      if (returnPort != null) {
        returnPort.send(result.toJson());
      }

      // If returnPort is null, then receiver died and
      // does not care about the result anymore. Just drop it.
    });
  });
}
