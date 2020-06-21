import 'dart:core';

class PebbleDevice{
  String name;
  int address;
  PebbleDevice(this.name, this.address);

  Map<String,dynamic> toJson() => {'name':name, 'address':address};
}