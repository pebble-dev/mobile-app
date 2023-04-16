import 'package:cobble/domain/api/auth/auth.dart';
import 'package:cobble/domain/api/auth/user.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_step.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class RebbleSetupSuccess extends HookConsumerWidget implements CobbleScreen {
  const RebbleSetupSuccess({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final preferences = ref.watch(preferencesProvider);
    final userFuture = ref.watch(authUserProvider.future);

    return CobbleScaffold.page(
      title: tr.setup.success.title,
      child: FutureBuilder(
        future: userFuture,
        builder: (context, snap) => CobbleStep(
            icon: const CompIcon(RebbleIcons.rocket80, RebbleIcons.rocket80_background, size: 80,),
            title: tr.setup.success.subtitle,
            child: Text(
              tr.setup.success.welcome(name: snap.hasData ? (snap.data! as User).name : "..."),
              textAlign: TextAlign.center,
            )
        ),
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
