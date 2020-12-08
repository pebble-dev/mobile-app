import 'dart:convert';

import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:shared_preferences/shared_preferences.dart';

class StoredDevice {
  bool isDefault;
  final PebbleScanDevice device;

  StoredDevice(this.device, [this.isDefault = false]);

  Map<String, dynamic> toJson() => {'isDefault': isDefault, 'device': device};
}

class PairedStorage extends StateNotifier<List<StoredDevice>> {
  PairedStorage() : super(List.empty()) {
    _readCurrent();
  }

  Future<SharedPreferences> get _prefs async => SharedPreferences.getInstance();

  Future<void> _readCurrent() async {
    if (await _prefs.then((value) => value.containsKey("pairList"))) {
      List<String> pairedJson =
          await _prefs.then((value) => value.getStringList("pairList"));
      List<StoredDevice> pairedList = pairedJson.map((e) {
        dynamic devRaw = jsonDecode(e);
        return _StoredDevice(
            PebbleScanDevice.stored(
                devRaw['device']['name'],
                devRaw['device']['address'],
                devRaw['device']['serialNumber'],
                devRaw['device']['color']),
            devRaw['isDefault']);
      }).toList();
      state = pairedList;
    } else
      state = List<StoredDevice>();
  }

  Future<void> _storeState() async {
    List<String> pairedJson = state.map((e) => jsonEncode(e)).toList();
    await _prefs.then((value) => value.setStringList("pairList", pairedJson));
  }

  static Future<void> register(PebbleScanDevice device,
          [bool isDefault = false]) async =>
      _writeNew((await _readCurrent())..add(_StoredDevice(device, isDefault)));

    if (!state.any((element) => element.isDefault)) {
      // Force newly registered device as default
      // if there is no existing default
      isDefault = true;
    }

  static Future<PebbleScanDevice> get(int address) async =>
      (await _readCurrent())
          .firstWhere((element) => element.device.address == address)
          .device;

  static Future<PebbleScanDevice> getDefault() async => (await _readCurrent())
      .firstWhere((element) => element.isDefault, orElse: () => null)
      ?.device;

  Future<void> unregister(int address) async {
    state = state.where((element) => element.device.address != address);
    await _storeState();
  }

  Future<void> setDefault(int address) async {
    state = state.map((element) =>
        StoredDevice(element.device, element.device.address == address));
    await _storeState();
  }
}

final pairedStorageProvider = StateNotifierProvider((ref) => PairedStorage());
final defaultWatchProvider = Provider((ref) => ref
    .watch(pairedStorageProvider.state)
    .firstWhere((element) => element.isDefault, orElse: () => null)
    ?.device);
