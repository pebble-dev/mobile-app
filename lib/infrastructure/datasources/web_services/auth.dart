import 'dart:async';

import 'package:cobble/domain/api/auth/oauth.dart';
import 'package:cobble/domain/api/auth/oauth_token.dart';
import 'package:cobble/domain/api/auth/user.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/web_services/service.dart';

const _cacheLifetime = Duration(minutes: 5);

class AuthService extends Service {
  static const String version = "v1";
  AuthService(String baseUrl, this._prefs, this._oauth, this._token)
      : super(baseUrl + "/" + version);
  final OAuthToken _token;
  final OAuthClient _oauth;
  final Preferences _prefs;

  User? _cachedUser;
  DateTime? _cacheAge;

  Future<User> get user async {
    final tokenCreationDate = _prefs.getOAuthTokenCreationDate();
    if (_cachedUser == null || _cacheAge == null ||
        DateTime.now().difference(_cacheAge!) >= _cacheLifetime) {
      _cacheAge = DateTime.now();
      final OAuthToken token;
      if (tokenCreationDate != null) {
        token = await _oauth.ensureNotStale(_token, tokenCreationDate);
      } else {
        Log.w("No token creation date found, using current token without ensuring it's not stale");
        token = _token;
      }
      User user = await client.getSerialized(User.fromJson, "me",
          token: token.accessToken);
      _cachedUser = user;
      return user;
    } else {
      return _cachedUser!;
    }
  }

  Future<void> signOut() async {
    await _oauth.signOut();
  }
}
