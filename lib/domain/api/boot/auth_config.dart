import 'package:cobble/domain/api/boot/base_url_entry.dart';
import 'package:json_annotation/json_annotation.dart';

part 'auth_config.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake)
class AuthConfig extends BaseURLEntry {
  final String authorizeUrl;
  final String refreshUrl;
  final String clientId;

  AuthConfig({
    required base,
    required this.authorizeUrl,
    required this.refreshUrl,
    required this.clientId
  }) : super(base);
  factory AuthConfig.fromJson(Map<String, dynamic> json) => _$AuthConfigFromJson(json);
  @override
  Map<String, dynamic> toJson() => _$AuthConfigToJson(this);

  @override
  String toString() => toJson().toString();
}