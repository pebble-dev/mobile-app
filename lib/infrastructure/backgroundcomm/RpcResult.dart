class RpcResult {
  final int id;
  final Object? successResult;
  final Object? errorResult;
  final StackTrace? errorStacktrace;

  RpcResult(
      this.id, this.successResult, this.errorResult, this.errorStacktrace);

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
