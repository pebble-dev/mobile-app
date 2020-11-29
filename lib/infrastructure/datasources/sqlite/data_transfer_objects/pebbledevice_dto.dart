import 'package:cobble/domain/entities/PebbleDevice.dart';
import 'package:cobble/infrastructure/datasources/sqlite/SqliteTables.dart';

extension PebbleDeviceSqlExtensions on PebbleDevice {
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
