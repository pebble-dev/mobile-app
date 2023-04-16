import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/ui/common/components/cobble_step.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class UpdatePrompt extends HookWidget implements CobbleScreen {
  const UpdatePrompt({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var connectionState = useProvider(connectionStateProvider.state);
    return CobbleScaffold.page(
        title: "Update",
        child: Container(
          padding: const EdgeInsets.all(16.0),
          alignment: Alignment.topCenter,
          child: CobbleStep(
            icon: const CompIcon(RebbleIcons.check_for_updates, RebbleIcons.check_for_updates_background, size: 80.0),
            title: "Checking for update...",
            child: Column(
              children: [
                const LinearProgressIndicator(),
                const SizedBox(height: 16.0),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(RebbleIcons.send_to_watch_unchecked),
                    const SizedBox(width: 8.0),
                    Text(connectionState.currentConnectedWatch?.name ?? "Watch"),
                  ],
                ),
              ],
            ),
          )
        ));
  }
}