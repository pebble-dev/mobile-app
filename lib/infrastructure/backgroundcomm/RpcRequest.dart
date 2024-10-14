import 'package:json_annotation/json_annotation.dart';

part 'RpcRequest.g.dart';


abstract class SerializableRpcRequest {
  Map<String, dynamic> toJson();
}

@JsonSerializable()
class RpcRequest extends SerializableRpcRequest {
  final int requestId;
  final String type;
  final Map<String, dynamic> input;

  RpcRequest(this.requestId, this.input, this.type);

  @override
  String toString() {
    return 'RpcRequest{requestId: $requestId, input: $input, type: $type}';
  }

  factory RpcRequest.fromJson(Map<String, dynamic> json) => _$RpcRequestFromJson(json);
  @override
  Map<String, dynamic> toJson() => _$RpcRequestToJson(this);
}
