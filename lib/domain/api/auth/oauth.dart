import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:math';

import 'package:cobble/domain/api/auth/oauth_token.dart';
import 'package:cobble/domain/api/boot/boot.dart';
import 'package:cobble/domain/api/status_exception.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/secure_storage.dart';
import 'package:crypto/crypto.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

const _redirectUri = "rebble://auth_complete";

class OAuthClient {
  final String authoriseUrl;
  final String refreshUrl;
  final String clientId;
  final Preferences _prefs;
  final SecureStorage _secureStorage;

  String? _lastState;
  String? _verifier;

  final HttpClient _client = HttpClient();

  OAuthClient(this._prefs, this._secureStorage, this.authoriseUrl,
      this.refreshUrl, this.clientId);

  String _generateState() {
    final random = Random.secure();
    final bytes = List<int>.generate(16, (_) => random.nextInt(256));
    final state = base64Url.encode(bytes).split("=")[0];
    _lastState = state;
    return state;
  }

  String _generateChallenge() {
    final random = Random.secure();
    final verifier = base64Url
        .encode(
          List<int>.generate(32, (_) => random.nextInt(256)),
        )
        .split("=")[0];
    final challenge = base64Url
        .encode(
          sha256.convert(ascii.encode(verifier)).bytes,
        )
        .split("=")[0];

    _verifier = verifier;
    return challenge;
  }

  String _generateCodeTokenRequest(
          String code, String clientId, String verifier, String redirectUri) =>
      Uri(
        queryParameters: {
          "grant_type": "authorization_code",
          "code": code,
          "client_id": clientId,
          "code_verifier": _verifier,
          "redirect_uri": _redirectUri,
        },
      ).query;

  String _generateRefreshTokenRequest(String refreshToken, String clientId) =>
      Uri(
        queryParameters: {
          "grant_type": "refresh_token",
          "refresh_token": refreshToken,
          "client_id": clientId,
        },
      ).query;

  Uri generateAuthoriseWebviewUrl() {
    final state = _generateState();
    final challenge = _generateChallenge();

    return Uri.parse(authoriseUrl).replace(
      queryParameters: {
        "response_type": "code",
        "client_id": clientId,
        "state": state,
        "code_challenge": challenge,
        "code_challenge_method": "S256",
        "redirect_uri": _redirectUri,
        "scope": "pebble profile"
      },
    );
  }

  Future<OAuthToken> requestTokenFromCode(String code, String state) async {
    if (state != _lastState) {
      throw OAuthException("_state_mismatch");
    }
    _lastState = null;

    final List<int> body = utf8.encode(_generateCodeTokenRequest(
      code,
      clientId,
      _verifier!,
      _redirectUri,
    ));
    _verifier = null;

    return _sendTokenRequest(body);
  }

  Future<OAuthToken> requestTokenFromRefresh(String refreshToken) async {
    final List<int> body = utf8.encode(_generateRefreshTokenRequest(
      refreshToken,
      clientId,
    ));

    return _sendTokenRequest(body);
  }

  Future<OAuthToken> _sendTokenRequest(List<int> body) async {
    final refreshUri = Uri.parse(refreshUrl);
    final query = Map<String, dynamic>.from(refreshUri.queryParameters);
    final req =
        await _client.postUrl(refreshUri.replace(queryParameters: query));
    req.headers.set("Content-Length", body.length.toString());
    req.headers.set("Content-Type", "application/x-www-form-urlencoded");
    req.headers.set("Accept", "application/json");
    req.add(body);
    final res = await req.close();
    Completer<Map<String, dynamic>> _completer =
        Completer<Map<String, dynamic>>();
    List<int> data = [];

    res.listen((event) {
      data.addAll(event);
    }, onDone: () {
      if (res.statusCode != 200) {
        try {
          Map<String, dynamic> body = jsonDecode(String.fromCharCodes(data));
          _completer.complete(body);
        } on FormatException {
          _completer.completeError(StatusException(res.statusCode,
              res.reasonPhrase + " (No usable JSON reason)", refreshUri));
        }
      } else {
        Map<String, dynamic> body = jsonDecode(String.fromCharCodes(data));
        _completer.complete(body);
      }
    }, onError: (error, stackTrace) {
      _completer.completeError(error, stackTrace);
    });

    final jsonBody = await _completer.future;
    if (jsonBody.containsKey("error")) {
      throw OAuthException(jsonBody["error"]);
    } else {
      final token = OAuthToken.fromJson(jsonBody);
      await _prefs.setOAuthTokenCreationDate(
          DateTime.now().subtract(const Duration(hours: 1)));
      await _secureStorage.setToken(token);
      return token;
    }
  }

  Future<OAuthToken> ensureNotStale(
      OAuthToken currentToken, DateTime tokenCreationDate) async {
    final lifetime = Duration(seconds: currentToken.expiresIn);
    if (DateTime.now().difference(tokenCreationDate) > lifetime) {
      return await requestTokenFromRefresh(currentToken.refreshToken);
    } else {
      return currentToken;
    }
  }
}

class OAuthException implements Exception {
  final String errorCode;

  OAuthException(this.errorCode);
  @override
  String toString() => "OAuthException: $errorCode";
}

final oauthClientProvider = FutureProvider((ref) async {
  final boot = await (await ref.watch(bootServiceProvider.future)).config;
  final prefs = await ref.watch(preferencesProvider.future);
  final secureStorage = ref.watch(secureStorageProvider);
  return OAuthClient(prefs, secureStorage, boot.auth.authoriseUrl,
      boot.auth.refreshUrl, boot.auth.clientId);
});
