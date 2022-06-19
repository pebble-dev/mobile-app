import 'dart:async';

import 'package:cobble/domain/api/auth/oauth.dart';
import 'package:cobble/domain/api/auth/oauth_token.dart';
import 'package:cobble/domain/api/auth/user.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/web_services/service.dart';

class AuthService extends Service {
  static const String version = "v1";
  AuthService(String baseUrl, this._prefs, this._oauth, this._token)
      : super(baseUrl + "/" + version);
  final OAuthToken _token;
  final OAuthClient _oauth;
  final Preferences _prefs;

  Future<User> get user async {
    final tokenCreationDate = _prefs.getOAuthTokenCreationDate();
    if (tokenCreationDate == null) {
      throw StateError("token creation date null when token exists");
    }
    final token = await _oauth.ensureNotStale(_token, tokenCreationDate);
    User user = await client.getSerialized(User.fromJson, "me",
        token: token.accessToken);
    return user;
  }
}
