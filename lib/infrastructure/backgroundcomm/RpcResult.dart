
import 'package:json_annotation/json_annotation.dart';

part 'RpcResult.g.dart';

@JsonSerializable()
class RpcResult {
  final int id;
  final String? successResult;
  final String? errorResult;

  RpcResult(
      this.id, this.successResult, this.errorResult);

  @override
  String toString() {
    return 'RpcResult{id: $id, '
        'successResult: $successResult, '
        'errorResult: $errorResult}';
  }

  static RpcResult success(int id, Object result) {
    return RpcResult(id, result.toString(), null);
  }

  static RpcResult error(int id, Object errorResult) {
    return RpcResult(id, null, errorResult.toString());
  }

  factory RpcResult.fromJson(Map<String, dynamic> json) => _$RpcResultFromJson(json);
  Map<String, dynamic> toJson() => _$RpcResultToJson(this);
}
