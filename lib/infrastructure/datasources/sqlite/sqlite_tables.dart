import 'package:cobble/domain/entities/pebble_scan_device.dart';

final Map<Type, String> typeToTable = {
  PebbleScanDevice: TableNames.pebbledevice,
};

class TableNames {
  static String get pebbledevice => "pebbledevice";
}

class SqlCreateTables {
  // id is the address of the device
  static String createPebbleDevice() => '''
    CREATE TABLE
      ${TableNames.pebbledevice} (
        id TEXT PRIMARY KEY,
        name TEXT
      )''';
}
