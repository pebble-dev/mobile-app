import 'dart:core';

import 'package:cobble/domain/entities/base_obj.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pebble_scan_device.g.dart';

@JsonSerializable()
class PebbleScanDevice extends BaseObj {
  String? name;
  int address;
  String? version;
  String? serialNumber;
  int? color;
  bool? runningPRF;
  bool? firstUse;

  PebbleScanDevice(this.name, this.address, this.version, this.serialNumber,
      this.color, this.runningPRF, this.firstUse);

  PebbleScanDevice.stored(
      this.name, this.address, this.serialNumber, this.color);

  Map<String, dynamic> toJson() => {
        'name': name,
        'address': address,
        'serialNumber': serialNumber,
        'color': color
      };

  PebbleScanDevice.fromMap(Map<dynamic, dynamic> pigeon)
      : this(
            pigeon["name"],
            pigeon["address"],
            pigeon["version"] != null ? pigeon["version"] : "",
            pigeon["serialNumber"] != null ? pigeon["serialNumber"] : "",
            pigeon["color"] != null ? pigeon["color"] : 0,
            pigeon["runningPRF"] != null ? pigeon["runningPRF"] : false,
            pigeon["firstUse"] != null ? pigeon["firstUse"] : false);
}
