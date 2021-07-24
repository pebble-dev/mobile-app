import 'dart:convert';

import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:collection/collection.dart' show IterableExtension;
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

class StoredDevice {
  bool? isDefault;
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
          await _prefs.then((value) => value.getStringList("pairList")!);
      List<StoredDevice> pairedList = pairedJson.map((e) {
        dynamic devRaw = jsonDecode(e);
        return StoredDevice(
            PebbleScanDevice.stored(
                devRaw['device']['name'],
                devRaw['device']['address'],
                devRaw['device']['serialNumber'],
                devRaw['device']['color']),
            devRaw['isDefault']);
      }).toList();
      state = pairedList;
    } else
      state = [];
  }

  Future<void> _storeState() async {
    List<String> pairedJson = state.map((e) => jsonEncode(e)).toList();
    await _prefs.then((value) => value.setStringList("pairList", pairedJson));
  }

  Future<void> register(PebbleScanDevice newDevice) async {
    //Only write device when it doesn't already exist
    if (state.any((element) => element.device.address == newDevice.address)) {
      return;
    }

    bool isDefault = false;
    if (!state.any((element) => element.isDefault!)) {
      // Force newly registered device as default
      // if there is no existing default
      isDefault = true;
    }

    final storedDevice = StoredDevice(newDevice, isDefault);

    state = [...state, storedDevice];
    await _storeState();
  }

  Future<void> unregister(int? address) async {
    state =
        state.where((element) => element.device.address != address).toList();
    await _storeState();
  }

  Future<void> setDefault(int address) async {
    state = state
        .map((element) =>
            StoredDevice(element.device, element.device.address == address))
        .toList();
    await _storeState();
  }

  Future<void> clearDefault() async {
    state =
        state.map((element) => StoredDevice(element.device, false)).toList();
    await _storeState();
  }
}

final pairedStorageProvider = StateNotifierProvider((ref) => PairedStorage());
final defaultWatchProvider = Provider((ref) => IterableExtension(ref
    .watch(pairedStorageProvider) as List)
    .firstWhereOrNull((element) => element.isDefault!)
    ?.device);
final ProviderFamily<PebbleScanDevice, dynamic>? specificWatchProvider =
    Provider.family(((ref, dynamic address) => IterableExtension(ref
        .watch(pairedStorageProvider))
        .firstWhereOrNull((element) => element.device.address == address)
        ?.device) as PebbleScanDevice Function(ProviderReference, dynamic));
