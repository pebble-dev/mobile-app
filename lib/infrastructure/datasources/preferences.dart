import 'dart:async';

import 'package:cobble/domain/preferences.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Preferences {
  final SharedPreferences _sharedPrefs;

  final _preferencesUpdateStream = StreamController<Preferences>.broadcast();

  Stream<Preferences> preferencesUpdateStream;

  Preferences(this._sharedPrefs) {
    preferencesUpdateStream = _preferencesUpdateStream.stream;

    _preferencesUpdateStream.add(this);
  }

  int getLastConnectedWatchAddress() {
    return _sharedPrefs.getInt("LAST_CONNECTED_WATCH");
  }

  Future<void> setLastConnectedWatchAddress(int value) async {
    await _sharedPrefs.setInt("LAST_CONNECTED_WATCH", value);
    _preferencesUpdateStream.add(this);
  }
}

final preferencesProvider = FutureProvider<Preferences>((ref) async {
  final sharedPreferences = await ref.watch(sharedPreferencesProvider);
  return Preferences(sharedPreferences);
});
