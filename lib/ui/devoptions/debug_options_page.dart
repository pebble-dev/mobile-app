import 'package:cobble/domain/api/auth/auth.dart';
import 'package:cobble/domain/api/auth/user.dart';
import 'package:cobble/domain/api/no_token_exception.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/web_services/auth.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class DebugOptionsPage extends HookConsumerWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final preferences = ref.watch(preferencesProvider);
    final bootUrl = ref.watch(bootUrlProvider).value ?? "";
    final shouldOverrideBoot =
        ref.watch(shouldOverrideBootProvider).value ?? false;
    final overrideBootUrl =
        ref.watch(overrideBootValueProvider).value ?? "";
    final calendarControl = ref.watch(calendarControlProvider);

    final bootUrlController = useTextEditingController();
    final bootOverrideUrlController = useTextEditingController();

    final DebugControl debug = DebugControl();
    final sensitiveLoggingEnabled = useState(false);
    useEffect(() {
      debug.getSensitiveLoggingEnabled().then((value) {
        sensitiveLoggingEnabled.value = value;
      });
      return null;
    }, []);

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
          SwitchListTile(
            contentPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 15),
            value: sensitiveLoggingEnabled.value,
            onChanged: (value) {
              debug.setSensitiveLoggingEnabled(value);
              sensitiveLoggingEnabled.value = value;
            },
            title: const Text("Enable sensitive data logging"),
            subtitle: const Text("Enables more in-depth logging at the cost of privacy (e.g. notification contents)"),
          ),
          CobbleButton(
            onPressed: () async {
              try {
                AuthService auth = await ref.read(authServiceProvider.future);
                User user = await auth.user;
                String id = user.uid.toString();
                String bootOverrideCount = user.bootOverrides?.length.toString() ?? "0";
                String subscribed = user.isSubscribed.toString();
                String timelineTtl = user.timelineTtl.toString();
                debug.collectLogs(
                  """
User ID: $id
Boot override count: $bootOverrideCount
Subscribed: $subscribed
Timeline TTL: $timelineTtl
                """,
                );
              } on NoTokenException catch (_) {
                debug.collectLogs("Not logged in");
              }
            },
            label: "Share application logs",
          ),
          const SizedBox(height: 20),
          CobbleButton(
            label: "Force calendar resync",
            onPressed: () async {
              calendarControl.requestCalendarSync(true);
            },
          ),
        ],
      ).toList()),
    );
  }
}
