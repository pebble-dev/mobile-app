import 'package:cobble/domain/api/auth/auth.dart';
import 'package:cobble/domain/api/auth/user.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class RebbleSetupSuccess extends HookWidget implements CobbleScreen {
  const RebbleSetupSuccess({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final preferences = useProvider(preferencesProvider);
    final auth = useProvider(authServiceProvider);
    final userFuture = auth.then((service) => service.user);

    return CobbleScaffold.page(
      title: tr.setup.success.title,
      child: Column(
        children: <Widget>[
          Text(
            tr.setup.success.subtitle,
            style: Theme.of(context).textTheme.headline3,
          ),
          FutureBuilder<User>(
            future: userFuture,
            builder: (BuildContext context, AsyncSnapshot<User> snapshot) {
              if (snapshot.hasData) {
                return Text(
                    tr.setup.success.welcome(name: snapshot.data!.name));
              } else {
                return Text(" ");
              }
            },
          )
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
          onPressed: () {
            preferences.when(data: (prefs) async {
              await prefs.setHasBeenConnected();
              await prefs.setWasSetupSuccessful(true);
              context.pushAndRemoveAllBelow(HomePage());
            }, loading: (){}, error: (e, s){});
          },
          label: Text(tr.setup.success.fab)),
    );
  }
}
