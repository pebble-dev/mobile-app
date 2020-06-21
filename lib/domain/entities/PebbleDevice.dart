import 'dart:core';
import 'package:fossil/domain/entities/BaseObj.dart';

class PebbleDevice extends BaseObj {
  String name;
  int address;
  PebbleDevice(this.name, this.address);

  Map<String, dynamic> toJson() => {'name': name, 'address': address};
}
