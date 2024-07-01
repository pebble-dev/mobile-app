import 'package:cobble/domain/calendar/calendar_list.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/domain/calendar/requests/delete_all_pins_request.dart';
import 'package:cobble/domain/permissions.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundRpc.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class Calendar extends HookConsumerWidget implements CobbleScreen {

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final calendars = ref.watch(calendarListProvider);
    final calendarSelector = ref.watch(calendarListProvider.notifier);
    final calendarControl = ref.watch(calendarControlProvider);
    final backgroundRpc = ref.watch(backgroundRpcProvider);

    final preferences = ref.watch(preferencesProvider);
    final permissionControl = ref.watch(permissionControlProvider);
    final permissionCheck = ref.watch(permissionCheckProvider);

    useEffect(() {
      Future.microtask(() async {
        if (!(await permissionCheck.hasCalendarPermission()).value!) {
          await permissionControl.requestCalendarPermission();
        }
      });
      return null;
    }, ["one-time"]);

    final calendarSyncEnabled = useState(false);
    useEffect(() {
      calendarControl.getCalendarSyncEnabled().then((value) {
        calendarSyncEnabled.value = value;
      });
    }, [calendarSyncEnabled]);

    Future<void> setCalendarSyncEnabled(bool value) async {
      await calendarControl.setCalendarSyncEnabled(value);
      calendarSyncEnabled.value = value;
      if (!value) {
        await calendarControl.deleteAllCalendarPins();
      }
    }

    return CobbleScaffold.tab(
      title: tr.calendar.title,
      child: ListView(
        children: [
          CobbleTile.setting(
            leading: RebbleIcons.calendar,
            title: tr.calendar.toggleTitle,
            subtitle: tr.calendar.toggleSubtitle,
            child: Switch(
              value: calendarSyncEnabled.value,
              onChanged: (value) async {
                await setCalendarSyncEnabled(value);
              },
            ),
          ),
          CobbleDivider(),
          if (calendarSyncEnabled.value) ...[
            CobbleTile.title(
              title: tr.calendar.choose,
            ),
            ...calendars.value?.map((e) {
              return CobbleTile.setting(
                leading: BoxDecoration(
                  color: Color(e.color).withOpacity(1),
                  shape: BoxShape.circle,
                ),
                    title: e.name,
                    child: Checkbox(
                      value: e.enabled,
                      onChanged: (enabled) {
                        calendarSelector.setCalendarEnabled(e.id, enabled!);
                        calendarControl.requestCalendarSync(false);
                      },
                    ),
                  );
                }).toList() ??
                [],
          ],
        ],
      ),
    );
  }
}
