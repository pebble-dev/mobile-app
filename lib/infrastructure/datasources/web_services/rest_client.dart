import 'dart:async';
import 'dart:convert';
import 'dart:io';

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
    HttpClientResponse res = await req.close();

    if (res.statusCode != 200) {
      _completer.completeError(StatusException(res.statusCode, res.reasonPhrase, requestUri));
    }else {
      List<int> data = [];
      res.listen((event) {
        data.addAll(event);
      }, onDone: () {
        Map<String, dynamic> body = jsonDecode(String.fromCharCodes(data));
        _completer.complete(modelJsonFactory(body));
      }, onError: (error, stackTrace) {
        _completer.completeError(error, stackTrace);
      });
    }

    return _completer.future;
  }
}

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