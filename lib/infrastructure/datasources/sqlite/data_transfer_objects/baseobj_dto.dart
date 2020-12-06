import 'package:cobble/domain/entities/base_obj.dart';
import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/infrastructure/datasources/sqlite/data_transfer_objects/pebbledevice_dto.dart';
import 'package:cobble/ui/exceptions/error_message.dart';

extension BaseObjSqlExtensions on BaseObj {
  dynamic get sqlMap {
    switch (this.runtimeType) {
      case PebbleScanDevice:
        return (this as PebbleScanDevice).sqlMap;
      default:
        throw ErrorMessage(
            message: "BaseObj SQL ERROR: Object Type Not Supported!!");
    }
  }
}
