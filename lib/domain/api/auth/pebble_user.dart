import 'package:json_annotation/json_annotation.dart';

part 'pebble_user.g.dart';

@JsonSerializable()
class PebbleUser {
  final String? email;
  final String id;
  final String name;

  PebbleUser({required this.id, required this.name, this.email});
  factory PebbleUser.fromJson(Map<String, dynamic> json) => _$PebbleUserFromJson(json);
  Map<String, dynamic> toJson() => _$PebbleUserToJson(this);

  @override
  String toString() => toJson().toString();
}