import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:shared_preferences/shared_preferences.dart';

class WSAuthUser {
  final String email;
  final String id;
  final String name;
  WSAuthUser(this.email, this.id, this.name);

  static Future<WSAuthUser> get() async {
    Map<String, dynamic> boot = await WSBoot.bootConf;
    Completer<WSAuthUser> _completer = new Completer<WSAuthUser>();
    if (boot != null) {
      HttpClient client = HttpClient();
      Uri userUri = Uri.parse(boot['config']['links']['authentication/me'] +
          "?access_token=${WSBoot.token}");
      HttpClientResponse res = await (await client.getUrl(userUri)).done;
      print(res.statusCode);
      res.listen((event) {
        print(String.fromCharCodes(event));
        Map<String, dynamic> user = jsonDecode(String.fromCharCodes(event));
        _completer
            .complete(WSAuthUser(user['email'], user['id'], user['name']));
      });
    } else
      _completer.complete(null);
    return _completer.future;
  }
}

class WSBoot {
  static Map<String, dynamic> _conf;
  static int _confExpiry = 0;
  static String token;
  static Future<Map<String, dynamic>> get bootConf async {
    Completer<Map<String, dynamic>> _completer =
        new Completer<Map<String, dynamic>>();
    if (_conf == null || DateTime.now().millisecondsSinceEpoch >= _confExpiry) {
      _confExpiry = DateTime.now().millisecondsSinceEpoch + (1000 * 60 * 60);

      SharedPreferences sp = await SharedPreferences.getInstance();
      if (!sp.containsKey("boot"))
        _completer.complete(null);
      else {
        HttpClient client = HttpClient();

        String bootUrl = sp.getString("boot");
        String params = bootUrl.substring(bootUrl.indexOf('?'));
        Uri actualUrl = Uri.parse(bootUrl.substring(0, bootUrl.indexOf('?')) +
            '/android/v3/1405/' +
            params); //TODO: iOS specific path when using iOS?
        token = actualUrl.queryParameters['access_token'];

        HttpClientResponse res = await (await client.getUrl(actualUrl)).done;
        res.listen((event) {
          _completer.complete(jsonDecode(String.fromCharCodes(event)));
        });
      }
    } else
      _completer.complete(_conf);
    return _completer.future;
  }
}
