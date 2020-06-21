import 'package:flutter/material.dart';
import 'package:fossil/ui/Localize.dart';
import 'package:fossil/ui/devoptions/DevOptionsPage.dart';
import 'package:fossil/ui/home/HomePage.dart';
import 'package:fossil/ui/setup/FirstRunPage.dart';
import 'package:fossil/ui/setup/MoreSetup.dart';
import 'package:fossil/ui/setup/PairPage.dart';
import 'package:fossil/ui/setup/boot/RebbleSetup.dart';
import 'package:fossil/ui/setup/boot/RebbleSetupFail.dart';
import 'package:fossil/ui/setup/boot/RebbleSetupSuccess.dart';
import 'package:fossil/ui/splash/SplashPage.dart';

class Router {
  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case '/':
      case '/splash':
        return MaterialPageRoute(builder: (_) => SplashPage());
      case '/home':
        return MaterialPageRoute(builder: (_) => HomePage());
      case '/devoptions':
        return MaterialPageRoute(builder: (_) => DevOptionsPage());
      case '/firstrun':
        return MaterialPageRoute(builder: (_) => FirstRunPage());
      case '/setupsuccess':
        return MaterialPageRoute(builder: (_) => RebbleSetupSuccess());
      case '/setupfail':
        return MaterialPageRoute(builder: (_) => RebbleSetupFail());
      case '/moresetup':
        return MaterialPageRoute(builder: (_) => MoreSetup());
      case '/moresetup':
        return MaterialPageRoute(builder: (_) => PairPage());
      case '/setup':
        return MaterialPageRoute(builder: (_) => RebbleSetup());

      default:
        return _errorRoute(settings);
    }
  }

  static Route<dynamic> _errorRoute(settings, {String message}) {
    return MaterialPageRoute(builder: (_) {
      return Scaffold(
        body: Center(
          child: Text(message ??
              "${Localize.it("No route defined for")} ${settings.name}"),
        ),
      );
    });
  }
}
