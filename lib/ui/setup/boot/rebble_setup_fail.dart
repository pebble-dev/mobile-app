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

class RebbleSetupFail extends HookWidget implements CobbleScreen {
  const RebbleSetupFail({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final preferences = useProvider(preferencesProvider);
    return CobbleScaffold.page(
      title: tr.setup.failure.title,
      child: CobbleStep(
        icon: const CompIcon(RebbleIcons.dead_watch_ghost80, RebbleIcons.dead_watch_ghost80_background, size: 80.0),
        title: tr.setup.failure.subtitle,
        subtitle: tr.setup.failure.error,
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
