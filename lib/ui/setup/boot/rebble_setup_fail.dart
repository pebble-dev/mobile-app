import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_circle.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class RebbleSetupFail extends HookWidget implements CobbleScreen {
  const RebbleSetupFail({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final preferences = useProvider(preferencesProvider);
    return CobbleScaffold.page(
      title: tr.setup.failure.title,
      child: Padding(
        padding: EdgeInsets.only(top: MediaQuery.of(context).size.height / 8, left: 8, right: 8),
        child: Column(
          children: <Widget>[
            CobbleCircle(
              child: const Image(
                image: AssetImage("images/app_large.png"),
              ),
              diameter: 120,
              color: Theme.of(context).primaryColor,
              padding: const EdgeInsets.all(20),
            ),
            const SizedBox(height: 16.0), // spacer
            Container(
              margin: const EdgeInsets.symmetric(vertical: 8),
              child: Text(
                tr.setup.failure.subtitle,
                style: Theme.of(context).textTheme.headline4,
                textAlign: TextAlign.center,
              ),
            ),
            const SizedBox(height: 24.0), // spacer
            Text(tr.setup.failure.error, textAlign: TextAlign.center),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
          onPressed: () async {
            await preferences.data?.value.setWasSetupSuccessful(false);
            context.pushAndRemoveAllBelow(HomePage());
          },
          label: Text(tr.setup.failure.fab)),
    );
  }
}
