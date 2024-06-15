import 'dart:io';

import 'package:flutter/foundation.dart';

class StatusException implements HttpException {
  final int statusCode;
  final String reason;
  final Uri _uri;
  StatusException(this.statusCode, this.reason, this._uri);
  @override
  String get message => "$statusCode $reason ${kDebugMode ? _uri.toString() : ""}";

  @override
  Uri? get uri => _uri;

  @override
  String toString() => "StatusException: $message";
}