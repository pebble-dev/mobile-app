import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/main.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/setup/first_run_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';

class SplashPage extends HookConsumerWidget {
  void Function() _openHome(
    bool hasBeenConnected, {
    required BuildContext context,
  }) =>
      () {
        if (!hasBeenConnected) {
          context.pushReplacement(FirstRunPage());
        } else {
          context.pushReplacement(HomePage());
        }
      };

  // ignore: unused_element
  void _askToBoot(bool hasBeenConnected, {required BuildContext context}) {
    showDialog(
        context: context,
        builder: (BuildContext context) {
          final openHome = _openHome(hasBeenConnected, context: context);
          return AlertDialog(
            title: Text(tr.splashPage.title),
            content: Text(tr.splashPage.body),
            actions: <Widget>[
              CobbleButton(
                outlined: false,
                label: tr.common.yes,
                onPressed: () {
                  canLaunch(getBootUrl).then((value) {
                    if (value)
                      launch(getBootUrl).then((_) => openHome());
                    else
                      openHome();
                  });
                },
              ),
              CobbleButton(
                outlined: false,
                label: tr.common.no,
                onPressed: openHome,
              ),
            ],
          );
        });
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final hasBeenConnected = ref.watch(hasBeenConnectedProvider).data;
    // Let's not do a timed splash screen here, it's a waste of
    // the user's time and there are better platform ways to do it
    useEffect(() {
      if (hasBeenConnected != null) {
        Future.microtask(_openHome(hasBeenConnected.value, context: context));
      }
    }, [hasBeenConnected]);
    return CobbleScaffold.page(
      child: Center(
        // This page shouldn't be visible for more than a split second, but if
        // it ever is, let the user know it's not broken
        child: CircularProgressIndicator(),
      ),
    );
  }
}
