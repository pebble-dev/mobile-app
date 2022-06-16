import 'package:json_annotation/json_annotation.dart';

import 'auth_config.dart';

part 'boot_config.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake)
class BootConfig {
  final AuthConfig auth;

  BootConfig({required this.auth});
  factory BootConfig.fromJson(Map<String, dynamic> json) => _$BootConfigFromJson(json);
  Map<String, dynamic> toJson() => _$BootConfigToJson(this);

  @override
  String toString() => toJson().toString();
}