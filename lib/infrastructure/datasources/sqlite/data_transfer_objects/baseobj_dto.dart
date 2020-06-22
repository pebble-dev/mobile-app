import 'package:fossil/domain/entities/BaseObj.dart';
import 'package:fossil/domain/entities/PebbleDevice.dart';
import 'package:fossil/ui/exceptions/ErrorMessage.dart';

import 'package:fossil/infrastructure/datasources/sqlite/data_transfer_objects/pebbledevice_dto.dart';

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
