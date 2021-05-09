import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/web_services.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

class RebbleSetupSuccess extends HookWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    final preferences = useProvider(preferencesProvider);
    return CobbleScaffold.page(
      title: tr.setup.success.title,
      child: Column(
        children: <Widget>[
          Text(
            tr.setup.success.subtitle,
            style: Theme.of(context).textTheme.headline3,
          ),
          FutureBuilder<WSAuthUser>(
            future: WSAuthUser.get(),
            builder:
                (BuildContext context, AsyncSnapshot<WSAuthUser> snapshot) {
              if (snapshot.hasData) {
                return Text(
                    tr.setup.success.welcome(name: snapshot.data!.name!));
              } else {
                return Text(" ");
              }
            },
          )
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
          onPressed: () {
            SharedPreferences.getInstance().then((prefs) async {
              await preferences.data?.value.setHasBeenConnected();
              prefs.setBool("bootSetup", true);
            }).then((_) {
              context.pushAndRemoveAllBelow(HomePage());
            });
          },
          label: Text(tr.setup.success.fab)),
    );
  }
}
