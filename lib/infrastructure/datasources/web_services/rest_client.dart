import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:cobble/domain/api/status_exception.dart';
import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';

typedef ModelJsonFactory<T> = T Function(Map<String, dynamic> json);

class RESTClient {
  final HttpClient _client = HttpClient();
  final Uri _baseUrl;
  final Logger _logger = Logger("REST");
  RESTClient(this._baseUrl);

  Future<T> getSerialized<T>(ModelJsonFactory<T> modelJsonFactory, String path, {Map<String, String>? params, String? token}) async {
    final body = await request(path: path, params: params, token: token);
    final T model = modelJsonFactory(jsonDecode(body));
    return model;
  }

  Future<String> request({required String path, String method = "GET", Map<String, String>? params, String? token}) async {
    Completer<String> _completer = Completer<String>();
    Uri requestUri = _baseUrl.replace(
      path: _baseUrl.pathSegments.join("/") + "/" + path,
      queryParameters: Map<String,String>.from(_baseUrl.queryParameters)
        ..addAll(params ?? {}),
    );

    HttpClientRequest req = await _client.getUrl(requestUri);
    if (token != null) {
      req.headers.add("Authorization", "Bearer $token");
    }
    if (kDebugMode) {
      req.followRedirects = false;
      _logger.finer("${req.method} ${req.uri} ${token != null ? "Authenticated" : "Anonymous"}");
    }
    HttpClientResponse res = await req.close();
    if (kDebugMode && res.isRedirect) { // handle redirects in debug keeping token
      req = await _client.openUrl(method, Uri.parse(res.headers.value("Location") ?? ""));
      if (token != null) {
        req.headers.add("Authorization", "Bearer $token");
      }
      res = await req.close();
    }
    if (res.statusCode < 200 || res.statusCode > 299) {
      _completer.completeError(StatusException(res.statusCode, res.reasonPhrase, requestUri));
    }else {
      List<int> data = [];
      res.listen((event) {
        data.addAll(event);
      }, onDone: () {
        _completer.complete(String.fromCharCodes(data));
      }, onError: (error, stackTrace) {
        _completer.completeError(error, stackTrace);
      });
    }
    return _completer.future;
  }
}