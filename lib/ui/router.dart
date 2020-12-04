import 'package:flutter/material.dart';
import 'package:cobble/ui/devoptions/dev_options_page.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/localize.dart';
import 'package:cobble/ui/setup/boot/rebble_setup.dart';
import 'package:cobble/ui/setup/boot/rebble_setup_fail.dart';
import 'package:cobble/ui/setup/boot/rebble_setup_success.dart';
import 'package:cobble/ui/setup/first_run_page.dart';
import 'package:cobble/ui/setup/more_setup.dart';
import 'package:cobble/ui/setup/pair_page.dart';
import 'package:cobble/ui/splash/splash_page.dart';

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
      case '/pair':
        return MaterialPageRoute(builder: (_) => PairPage());

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
