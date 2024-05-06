class RpcResult {
  final int id;
  final Object? successResult;
  final Object? errorResult;
  final StackTrace? errorStacktrace;

  RpcResult(
      this.id, this.successResult, this.errorResult, this.errorStacktrace);

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
      map['errorStacktrace'] != null
          ? StackTrace.fromString(map['errorStacktrace'] as String)
          : null,
    );
  }

  @override
  String toString() {
    return 'RpcResult{id: $id, '
        'successResult: $successResult, '
        'errorResult: $errorResult,'
        ' errorStacktrace: $errorStacktrace}';
  }

  static RpcResult success(int id, Object result) {
    return RpcResult(id, result, null, null);
  }

  static RpcResult error(int id, Object errorResult, StackTrace? stackTrace) {
    return RpcResult(id, null, errorResult, stackTrace);
  }
}
