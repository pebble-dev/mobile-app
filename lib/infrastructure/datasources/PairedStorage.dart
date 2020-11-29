import 'dart:convert';
import 'package:cobble/domain/entities/PebbleDevice.dart';
import 'package:shared_preferences/shared_preferences.dart';

class _StoredDevice {
  bool isDefault;
  final PebbleDevice device;
  _StoredDevice(this.device, [this.isDefault = false]);
  Map<String, dynamic> toJson() => {'isDefault': isDefault, 'device': device};
}

class PairedStorage {
  static Future<SharedPreferences> get _prefs async =>
      SharedPreferences.getInstance();

  static Future<List<_StoredDevice>> _readCurrent() async {
    if (await _prefs.then((value) => value.containsKey("pairList"))) {
      List<String> pairedJson =
          await _prefs.then((value) => value.getStringList("pairList"));
      List<_StoredDevice> pairedList = pairedJson.map((e) {
        dynamic devRaw = jsonDecode(e);
        return _StoredDevice(
            PebbleDevice(devRaw['device']['name'], devRaw['device']['address']),
            devRaw['isDefault']);
      }).toList();
      return pairedList;
    } else
      return List<_StoredDevice>();
  }

  static Future<void> _writeNew(List<_StoredDevice> newPaired) async {
    List<String> pairedJson = newPaired.map((e) => jsonEncode(e)).toList();
    await _prefs.then((value) => value.setStringList("pairList", pairedJson));
  }

  static Future<void> register(PebbleDevice device,
          [bool isDefault = false]) async =>
      _writeNew((await _readCurrent())..add(_StoredDevice(device, isDefault)));

  static Future<void> unregister(int address) async =>
      _writeNew((await _readCurrent())
        ..removeWhere((element) => element.device.address == address));

  static Future<PebbleDevice> get(int address) async => (await _readCurrent())
      .firstWhere((element) => element.device.address == address)
      .device;

  static Future<PebbleDevice> getDefault() async => (await _readCurrent())
      .firstWhere((element) => element.isDefault, orElse: () => null)
      ?.device;

  static Future<void> setDefault(int address) async =>
      _writeNew((await _readCurrent())
        ..firstWhere((element) => element.device.address == address).isDefault =
            true);
}
