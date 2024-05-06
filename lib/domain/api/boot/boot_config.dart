import 'package:cobble/domain/api/boot/base_url_entry.dart';
import 'package:cobble/domain/api/boot/webview_config.dart';
import 'package:json_annotation/json_annotation.dart';

import 'auth_config.dart';

part 'boot_config.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake)
class BootConfig {
  final AuthConfig auth;
  final BaseURLEntry appstore;
  final WebviewConfig webviews;

  BootConfig({required this.auth, required this.appstore, required this.webviews});
  factory BootConfig.fromJson(Map<String, dynamic> json) => _$BootConfigFromJson(json);
  Map<String, dynamic> toJson() => _$BootConfigToJson(this);

  @override
  String toString() => toJson().toString();
}