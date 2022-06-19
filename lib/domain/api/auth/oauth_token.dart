import 'package:json_annotation/json_annotation.dart';

part 'oauth_token.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake)
class OAuthToken {
  final String accessToken;
  final int expiresIn;
  final String tokenType;
  final String scope;
  final String refreshToken;

  OAuthToken({
    required this.accessToken,
    required this.expiresIn,
    required this.tokenType,
    required this.scope,
    required this.refreshToken,
  });
  factory OAuthToken.fromJson(Map<String, dynamic> json) => _$OAuthTokenFromJson(json);
  Map<String, dynamic> toJson() => _$OAuthTokenToJson(this);

  @override
  String toString() => toJson().toString();
}