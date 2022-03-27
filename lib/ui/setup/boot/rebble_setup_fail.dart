import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class RebbleSetupFail extends HookConsumerWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final preferences = ref.watch(preferencesProvider);
    return CobbleScaffold.page(
      title: "Activate Rebble services",
      child: Column(
        children: <Widget>[
          Text(
            "Oops!",
            style: Theme.of(context).textTheme.headline3,
          ),
          Text(
              "An error occured setting up Rebble, we'll load in offline mode and you can try again from settings later!")
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
          onPressed: () async {
            await preferences.data?.value.setWasSetupSuccessful(false);
            context.pushAndRemoveAllBelow(HomePage());
          },
          label: Text("OKAY")),
    );
  }
}
