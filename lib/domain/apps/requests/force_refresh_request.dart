class ForceRefreshRequest {
  final bool clear;

  ForceRefreshRequest(this.clear);

  Map<String, dynamic> toMap() {
    return {'type': 'ForceRefreshRequest', 'clear': clear};
  }

  factory ForceRefreshRequest.fromMap(Map<String, dynamic> map) {
    return ForceRefreshRequest(map['clear'] as bool);
  }
}
