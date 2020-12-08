import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/infrastructure/datasources/sqlite/sqlite_tables.dart';

extension PebbleDeviceSqlExtensions on PebbleScanDevice {
  Map<String, dynamic> get sqlMap {
    return {
      TableNames.pebbledevice: [this._sqlSelf],
    };
  }

  Map<String, dynamic> get _sqlSelf {
    if (this == null) return {};
    return {
      "id": this.address,
      "name": this.name,
    };
  }
}
