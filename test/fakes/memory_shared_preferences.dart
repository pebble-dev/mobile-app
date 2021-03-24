import 'package:shared_preferences/shared_preferences.dart';

/// Fake implementation of Shared Preferences that stores data in memory
/// Used for unit tests
class MemorySharedPreferences implements SharedPreferences {
  Map<String, dynamic> _data = {};

  @override
  Future<bool> clear() {
    _data.clear();
    return Future.value(true);
  }

  @override
  Future<bool> commit() {
    return Future.value(true);
  }

  @override
  bool containsKey(String key) {
    return _data.containsKey(key);
  }

  @override
  dynamic get(String key) {
    return _data[key];
  }

  @override
  bool? getBool(String key) {
    return _data[key];
  }

  @override
  double? getDouble(String key) {
    return _data[key];
  }

  @override
  int? getInt(String key) {
    return _data[key];
  }

  @override
  Set<String> getKeys() {
    return _data.keys.toSet();
  }

  @override
  String? getString(String key) {
    return _data[key];
  }

  @override
  List<String>? getStringList(String key) {
    return _data[key];
  }

  @override
  Future<void> reload() {
    return Future.value();
  }

  @override
  Future<bool> remove(String key) {
    _data.remove(key);
    return Future.value(true);
  }

  @override
  Future<bool> setBool(String key, bool value) {
    _data[key] = value;
    return Future.value(true);
  }

  @override
  Future<bool> setDouble(String key, double value) {
    _data[key] = value;
    return Future.value(true);
  }

  @override
  Future<bool> setInt(String key, int value) {
    _data[key] = value;
    return Future.value(true);
  }

  @override
  Future<bool> setString(String key, String value) {
    _data[key] = value;
    return Future.value(true);
  }

  @override
  Future<bool> setStringList(String key, List<String> value) {
    _data[key] = value;
    return Future.value(true);
  }
}
