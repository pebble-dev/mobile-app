import 'dart:core';
import 'package:fossil/domain/entities/BaseObj.dart';

class PebbleDevice extends BaseObj {
  String name;
  int address;
  String version;
  String serialNumber;
  int color;
  bool runningPRF;
  bool firstUse;
  PebbleDevice(this.name, this.address, this.version, this.serialNumber, this.color, this.runningPRF, this.firstUse);
  PebbleDevice.stored(this.name, this.address, this.serialNumber, this.color);
  Map<String, dynamic> toJson() => {'name': name, 'address': address, 'serialNumber': serialNumber, 'color': color};
}
