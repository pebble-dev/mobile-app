import 'package:cobble/domain/apps/requests/force_refresh_request.dart';
import 'package:cobble/domain/apps/requests/app_reorder_request.dart';

class RpcRequest {
  final int requestId;
  final Object input;

  RpcRequest(this.requestId, this.input);

  Map<String, dynamic> toMap() {
    return {
      'type': 'RpcRequest',
      'requestId': requestId,
      'input': _mapInput(),
    };
  }

  factory RpcRequest.fromMap(Map<String, dynamic> map) {
    final String type = map['type'] as String;
    if (type == 'RpcRequest') {
      return RpcRequest(
        map['requestId'] as int,
        _createInputFromMap(map['input']),
      );
    }
    throw ArgumentError('Invalid type: $type');
  }

  dynamic _mapInput() {
    if (input is ForceRefreshRequest) {
      return {'type': 'ForceRefreshRequest', 'data': (input as ForceRefreshRequest).toMap()};
    } else if (input is AppReorderRequest) {
      return {'type': 'AppReorderRequest', 'data': (input as AppReorderRequest).toMap()};
    }
    throw ArgumentError('Unsupported input type: ${input.runtimeType}');
  }

  static Object _createInputFromMap(Map<String, dynamic> map) {
    final String type = map['type'] as String;
    final Map<String, dynamic> data = map['data'] as Map<String, dynamic>;

    switch (type) {
      case 'ForceRefreshRequest':
        return ForceRefreshRequest.fromMap(data);
      case 'AppReorderRequest':
        return AppReorderRequest.fromMap(data);
      default:
        throw ArgumentError('Invalid input type: $type');
    }
  }

  @override
  String toString() {
    return 'RpcRequest{requestId: $requestId, input: $input}';
  }
}
