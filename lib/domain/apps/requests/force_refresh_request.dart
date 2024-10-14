import 'package:cobble/infrastructure/backgroundcomm/RpcRequest.dart';
import 'package:json_annotation/json_annotation.dart';

part 'force_refresh_request.g.dart';

@JsonSerializable()
class ForceRefreshRequest extends SerializableRpcRequest {
  final bool clear;

  ForceRefreshRequest(this.clear);

  @override
  String toString() {
    return 'ForceRefreshRequest{clear: $clear}';
  }

  factory ForceRefreshRequest.fromJson(Map<String, dynamic> json) => _$ForceRefreshRequestFromJson(json);
  @override
  Map<String, dynamic> toJson() => _$ForceRefreshRequestToJson(this);
}
