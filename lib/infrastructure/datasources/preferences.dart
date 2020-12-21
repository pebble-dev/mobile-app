import 'package:cobble/domain/preferences.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Preferences {
  final Future<SharedPreferences> _sharedPreferences;

  Preferences(this._sharedPreferences);

  Future<int> getLastConnectedWatchAddress() async {
    final sharedPrefs = await _sharedPreferences;
    await sharedPrefs.reload();

    return sharedPrefs.getInt("LAST_CONNECTED_WATCH");
  }

  Future<void> setLastConnectedWatchAddress(int value) async {
    final sharedPrefs = await _sharedPreferences;

    return sharedPrefs.setInt("LAST_CONNECTED_WATCH", value);
  }
}

final preferencesProvider = Provider<Preferences>((ref) {
  final sharedPreferences = ref.watch(sharedPreferencesProvider);
  return Preferences(sharedPreferences);
});
