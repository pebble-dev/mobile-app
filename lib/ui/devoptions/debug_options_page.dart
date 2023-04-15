import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class DebugOptionsPage extends HookWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    final preferences = useProvider(preferencesProvider);
    final bootUrl = useProvider(bootUrlProvider).data?.value ?? "";
    final shouldOverrideBoot =
        useProvider(shouldOverrideBootProvider).data?.value ?? false;
    final overrideBootUrl =
        useProvider(overrideBootValueProvider).data?.value ?? "";

    final bootUrlController = useTextEditingController();
    final bootOverrideUrlController = useTextEditingController();

    final DebugControl debug = DebugControl();

    useEffect(() {
      bootUrlController.text = bootUrl;
    }, [bootUrl]);

    useEffect(() {
      bootOverrideUrlController.text = overrideBootUrl;
    }, [overrideBootUrl]);

    return CobbleScaffold.page(
      title: "App Debug Options",
      child: ListView(
          children: ListTile.divideTiles(
        context: context,
        tiles: <Widget>[
          ListTile(
              contentPadding:
                  EdgeInsets.symmetric(vertical: 10, horizontal: 15),
              title: Text(
                "Boot",
                style: TextStyle(fontSize: 25),
              )),
          ListTile(
            contentPadding: EdgeInsets.symmetric(vertical: 10, horizontal: 15),
            title: Text("URL"),
            subtitle: TextField(
              controller: bootUrlController,
              readOnly: true,
            ),
          ),
          SwitchListTile(
              value: shouldOverrideBoot,
              title: Text("Override boot URL"),
              subtitle: Text("If enabled, will use the override boot URL instead of the main boot URL"),
              onChanged: (value) {
                preferences
                    .whenData((prefs) => prefs.setShouldOverrideBoot(value));
              }),
          ListTile(
            contentPadding: EdgeInsets.symmetric(vertical: 10, horizontal: 15),
            title: Text("Stage2 Override"),
            subtitle: Column(
              children: <Widget>[
                TextField(
                  controller: bootOverrideUrlController,
                  maxLines: 8,
                  minLines: 4,
                ),
                Container(
                    alignment: Alignment.centerRight,
                    child: ElevatedButton(
                      child: Text("Save"),
                      onPressed: () {
                        preferences.whenData((prefs) =>
                            prefs.setOverrideBootValue(
                                bootOverrideUrlController.text));
                      },
                    ))
              ],
            ),
          ),
          CobbleButton(
            onPressed: () => debug.collectLogs(),
            label: "Share application logs",
          ),
        ],
      ).toList()),
    );
  }
}
