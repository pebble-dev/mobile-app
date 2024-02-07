import 'dart:io';

class StatusException implements HttpException {
  final int statusCode;
  final String reason;
  final Uri _uri;
  StatusException(this.statusCode, this.reason, this._uri);
  @override
  String get message => "$statusCode $reason";

  @override
  Uri? get uri => _uri;

  @override
  String toString() => "StatusException: $message";
}