import 'package:json_annotation/json_annotation.dart';
import 'base_url_entry.dart';

part 'boot_config.g.dart';

@JsonSerializable()
class BootConfig {
  final BaseURLEntry auth;

  BootConfig({required this.auth});
  factory BootConfig.fromJson(Map<String, dynamic> json) => _$BootConfigFromJson(json);
}