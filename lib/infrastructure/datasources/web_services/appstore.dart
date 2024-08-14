import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/domain/api/auth/oauth.dart';
import 'package:cobble/domain/api/auth/oauth_token.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/web_services/service.dart';

class AppstoreService extends Service {
  static const String version = "v1";
  AppstoreService(String baseUrl, this._prefs, this._oauth, this._token)
      : super(baseUrl + "/" + version);
  final OAuthToken _token;
  final OAuthClient _oauth;
  final Preferences _prefs;

  Future<List<LockerEntry>> get locker async {
    final tokenCreationDate = _prefs.getOAuthTokenCreationDate();
    final OAuthToken token;
    if (tokenCreationDate != null) {
      token = await _oauth.ensureNotStale(_token, tokenCreationDate);
    } else {
      Log.w("No token creation date found, using current token without ensuring it's not stale");
      token = _token;
    }
    List<LockerEntry> entries = await client.getSerialized(
      (body) => (body["applications"] as List<dynamic>)
        .map((e) => e as Map<String, dynamic>)
        .map(LockerEntry.fromJson)
        .toList(),
      "locker",
      token: token.accessToken,
    );
    return entries;
  }

  Future<void> addToLocker(String uuid) async {
    final tokenCreationDate = _prefs.getOAuthTokenCreationDate();
    final OAuthToken token;
    if (tokenCreationDate != null) {
      token = await _oauth.ensureNotStale(_token, tokenCreationDate);
    } else {
      Log.w("No token creation date found, using current token without ensuring it's not stale");
      token = _token;
    }
    await client.request(
      path: "locker/$uuid",
      method: "PUT",
      token: token.accessToken,
    );
  }

  Future<void> removeFromLocker(String uuid) async {
    final tokenCreationDate = _prefs.getOAuthTokenCreationDate();
    final OAuthToken token;
    if (tokenCreationDate != null) {
      token = await _oauth.ensureNotStale(_token, tokenCreationDate);
    } else {
      Log.w("No token creation date found, using current token without ensuring it's not stale");
      token = _token;
    }
    await client.request(
      path: "locker/$uuid",
      method: "DELETE",
      token: token.accessToken,
    );
  }
}
