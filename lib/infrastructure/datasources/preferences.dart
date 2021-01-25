import 'dart:async';
import 'package:cobble/domain/preferences.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:stream_transform/stream_transform.dart';

class Preferences {
  final SharedPreferences _sharedPrefs;

  StreamController<Preferences> _preferencesUpdateStream;
  Stream<Preferences> preferencesUpdateStream;

  Preferences(this._sharedPrefs) {
    _preferencesUpdateStream = StreamController<Preferences>.broadcast();

    preferencesUpdateStream = _preferencesUpdateStream.stream;
  }

  int getLastConnectedWatchAddress() {
    return _sharedPrefs.getInt("LAST_CONNECTED_WATCH");
  }

  Future<void> setLastConnectedWatchAddress(int value) async {
    await _sharedPrefs.setInt("LAST_CONNECTED_WATCH", value);
    _preferencesUpdateStream.add(this);
  }

  bool isCalendarSyncEnabled() {
    return _sharedPrefs.getBool("ENABLE_CALENDAR_SYNC");
  }

  Future<void> setCalendarSyncEnabled(bool value) async {
    await _sharedPrefs.setBool("ENABLE_CALENDAR_SYNC", value);
    _preferencesUpdateStream.add(this);
  }
}

final preferencesProvider = FutureProvider<Preferences>((ref) async {
  final sharedPreferences = await ref.watch(sharedPreferencesProvider);
  return Preferences(sharedPreferences);
});

final calendarSyncEnabledProvider = _createPreferenceProvider(
  (preferences) => preferences.isCalendarSyncEnabled(),
);

final phoneNotificationsMuteProvider = _createPreferenceProvider(
  (preferences) => preferences.isPhoneNotificationMuteEnabled(),
);

final phoneCallsMuteProvider = _createPreferenceProvider(
  (preferences) => preferences.isPhoneCallMuteEnabled(),
);

StreamProvider<T> _createPreferenceProvider<T>(
  T Function(Preferences preferences) mapper,
) {
  return StreamProvider<T>((ref) {
    final preferences = ref.watch(preferencesProvider);

    return preferences.map(
        data: (preferences) => preferences.value.preferencesUpdateStream
            .startWith(preferences.value)
            .map(mapper)
            .distinct(),
        loading: (loading) => Stream.empty(),
        error: (error) => Stream.empty());
  });
}
