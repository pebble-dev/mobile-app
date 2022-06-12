import 'package:cobble/domain/api/boot/base_url_entry.dart';
import 'package:json_annotation/json_annotation.dart';

part 'auth_config.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake)
class AuthConfig extends BaseURLEntry {
  final String authoriseUrl;
  final String refreshUrl;

  AuthConfig({
    required base,
    required this.authoriseUrl,
    required this.refreshUrl
  }) : super(base);
  factory AuthConfig.fromJson(Map<String, dynamic> json) => _$AuthConfigFromJson(json);
  @override
  Map<String, dynamic> toJson() => _$AuthConfigToJson(this);

  @override
  String toString() => toJson().toString();
}