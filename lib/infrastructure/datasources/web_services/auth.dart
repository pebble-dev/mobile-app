import 'dart:async';

import 'package:cobble/domain/api/auth/user.dart';
import 'package:cobble/infrastructure/datasources/web_services/service.dart';

class AuthService extends Service {
  AuthService(String baseUrl, this._token) : super(baseUrl);
  final String _token;

  Future<User> get user async {
    User user = await client.getSerialized(User.fromJson, "me", token: _token);
    return user;
  }
}