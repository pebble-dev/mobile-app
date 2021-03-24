import 'dart:async';

import 'package:cobble/domain/preferences.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:stream_transform/stream_transform.dart';

class Preferences {
  final SharedPreferences _sharedPrefs;

  late StreamController<Preferences> _preferencesUpdateStream;
  late Stream<Preferences> preferencesUpdateStream;

  Preferences(this._sharedPrefs) {
    _preferencesUpdateStream = StreamController<Preferences>.broadcast();

    preferencesUpdateStream = _preferencesUpdateStream.stream;
  }

  int? getLastConnectedWatchAddress() {
    return _sharedPrefs.getInt("LAST_CONNECTED_WATCH");
  }

  Future<void> setLastConnectedWatchAddress(int value) async {
    await _sharedPrefs.setInt("LAST_CONNECTED_WATCH", value);
    _preferencesUpdateStream.add(this);
  }

  bool? isCalendarSyncEnabled() {
    return _sharedPrefs.getBool("ENABLE_CALENDAR_SYNC");
  }

  Future<void> setCalendarSyncEnabled(bool value) async {
    await _sharedPrefs.setBool("ENABLE_CALENDAR_SYNC", value);
    _preferencesUpdateStream.add(this);
  }

  bool? isPhoneNotificationMuteEnabled() {
    return _sharedPrefs.getBool("MUTE_PHONE_NOTIFICATIONS");
  }

  bool isWorkaroundDisabled(String workaround) {
    return _sharedPrefs.getBool("DISABLE_WORKAROUND_" + workaround) ?? false;
  }

  Future<void> setPhoneNotificationMute(bool value) async {
    await _sharedPrefs.setBool("MUTE_PHONE_NOTIFICATIONS", value);
    _preferencesUpdateStream.add(this);
  }

  bool? isPhoneCallMuteEnabled() {
    return _sharedPrefs.getBool("MUTE_PHONE_CALLS");
  }

  Future<void> setPhoneCallsMute(bool value) async {
    await _sharedPrefs.setBool("MUTE_PHONE_CALLS", value);
    _preferencesUpdateStream.add(this);
  }

  Future<void> setWorkaroundDisabled(String workaround, bool disabled) async {
    await _sharedPrefs.setBool("DISABLE_WORKAROUND_" + workaround, disabled);
    _preferencesUpdateStream.add(this);
  }

  bool? areNotificationsEnabled() {
    return _sharedPrefs.getBool("MASTER_NOTIFICATION_TOGGLE");
  }

  Future<void> setNotificationsEnabled(bool value) async {
    await _sharedPrefs.setBool("MASTER_NOTIFICATION_TOGGLE", value);
    _preferencesUpdateStream.add(this);
  }

  List<String?>? getNotificationsMutedPackages() {
    return _sharedPrefs.getStringList("MUTED_NOTIF_PACKAGES");
  }

  Future<void> setNotificationsMutedPackages(List<String?> packages) async {
    await _sharedPrefs.setStringList("MUTED_NOTIF_PACKAGES", packages as List<String>);
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

final notificationToggleProvider = _createPreferenceProvider(
  (preferences) => preferences.areNotificationsEnabled(),
);

final notificationsMutedPackagesProvider = _createPreferenceProvider(
  (preferences) => preferences.getNotificationsMutedPackages(),
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
