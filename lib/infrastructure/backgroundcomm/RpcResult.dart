
import 'package:json_annotation/json_annotation.dart';

part 'RpcResult.g.dart';

@JsonSerializable()
class RpcResult {
  final int id;
  final String? successResult;
  final String? errorResult;
  final String? errorStacktrace;

  RpcResult(
      this.id, this.successResult, this.errorResult, [this.errorStacktrace]);

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'successResult': successResult,
      'errorResult': errorResult,
      'errorStacktrace': errorStacktrace?.toString(),
    };
  }

  static RpcResult fromMap(Map<String, dynamic> map) {
    return RpcResult(
      map['id'] as int,
      map['successResult'],
      map['errorResult'],
      map['errorStacktrace']
    );
  }

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
