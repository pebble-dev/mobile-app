import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:cobble/domain/api/status_exception.dart';
import 'package:flutter/foundation.dart';

class RESTClient {
  final HttpClient _client = HttpClient();
  final Uri _baseUrl;
  RESTClient(this._baseUrl);

  Future<T> getSerialized<T>(Function modelJsonFactory, String path, {Map<String, String>? params, String? token}) async {
    Completer<T> _completer = Completer<T>();
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
      print("[REST] ${req.method} ${req.uri} ${token != null ? "Authenticated" : "Anonymous"}");
    }
    HttpClientResponse res = await req.close();
    if (kDebugMode && res.isRedirect) { // handle redirects in debug keeping token
      req = await _client.getUrl(Uri.parse(res.headers.value("Location") ?? ""));
      if (token != null) {
        req.headers.add("Authorization", "Bearer $token");
      }
      res = await req.close();
    }
    if (res.statusCode != 200) {
      _completer.completeError(StatusException(res.statusCode, res.reasonPhrase, requestUri));
    }else {
      List<int> data = [];
      res.listen((event) {
        data.addAll(event);
      }, onDone: () {
        Map<String, dynamic> body = jsonDecode(String.fromCharCodes(data));
        print(body);
        _completer.complete(modelJsonFactory(body));
      }, onError: (error, stackTrace) {
        _completer.completeError(error, stackTrace);
      });
    }

    return _completer.future;
  }
}