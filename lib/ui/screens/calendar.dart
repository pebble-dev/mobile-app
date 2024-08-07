import 'package:cobble/domain/calendar/calendar_list_provider.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/domain/permissions.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundRpc.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:collection/collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../infrastructure/pigeons/pigeons.g.dart';

class Calendar extends HookConsumerWidget implements CobbleScreen {
  const Calendar({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final calendarControl = ref.watch(calendarControlProvider);
    final calendarList = ref.watch(calendarListProvider);

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

    final calendarElements = groupBy(calendarList, (e) => e.account).entries.map((grp) {
      return [
        CobbleTile.sectionTitle(key: ValueKey("head${grp.key}"), title: grp.key),
        ...grp.value.map(
                (c) => _CalendarSettings(
                    key: ValueKey(c.id),
                    calendarPigeon: c,
                    calendarControl: calendarControl,
                ),
        ),
      ];
    }).toList();

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
            ...calendarElements.flattened,
          ],
        ],
      ),
    );
  }
}


class _CalendarSettings extends StatelessWidget {
  final CalendarPigeon calendarPigeon;
  final CalendarControl calendarControl;
  const _CalendarSettings({Key? key, required this.calendarPigeon, required this.calendarControl}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return CobbleTile.setting(
      leading: BoxDecoration(
        color: Color(calendarPigeon.color).withOpacity(1),
        shape: BoxShape.circle,
      ),
      title: calendarPigeon.name,
      child: Checkbox(
        value: calendarPigeon.enabled,
        onChanged: (enabled) {
          calendarControl.setCalendarEnabled(calendarPigeon.id, enabled!);
        },
      ),
    );
  }
}