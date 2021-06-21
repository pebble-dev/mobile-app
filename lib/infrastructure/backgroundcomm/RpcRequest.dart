class RpcRequest {
  final int requestId;
  final Object input;

  RpcRequest(this.requestId, this.input);

  @override
  String toString() {
    return 'RpcRequest{requestId: $requestId, input: $input}';
  }
}
