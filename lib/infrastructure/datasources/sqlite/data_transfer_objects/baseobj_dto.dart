import 'package:cobble/domain/entities/BaseObj.dart';
import 'package:cobble/domain/entities/PebbleDevice.dart';
import 'package:cobble/ui/exceptions/ErrorMessage.dart';

import 'package:cobble/infrastructure/datasources/sqlite/data_transfer_objects/pebbledevice_dto.dart';

extension BaseObjSqlExtensions on BaseObj {
  dynamic get sqlMap {
    switch (this.runtimeType) {
      case PebbleDevice:
        return (this as PebbleDevice).sqlMap;
      default:
        throw ErrorMessage(
            message: "BaseObj SQL ERROR: Object Type Not Supported!!");
    }
  }
}
