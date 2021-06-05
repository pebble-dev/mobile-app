import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/domain/calendar/calendar_list.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:cobble/domain/permissions.dart';

class Calendar extends HookWidget implements CobbleScreen {

  @override
  Widget build(BuildContext context) {
    final calendars = useProvider(calendarListProvider.state);
    final calendarSelector = useProvider(calendarListProvider);
    final calendarControl = useProvider(calendarControlProvider);
  
    final preferences = useProvider(preferencesProvider);
    final calendarSyncEnabled = useProvider(calendarSyncEnabledProvider);
    final permissionControl = useProvider(permissionControlProvider);
    final permissionCheck = useProvider(permissionCheckProvider);
    
    useEffect(() {
      Future.microtask(() async {
        if (!(await permissionCheck.hasCalendarPermission()).value!) {
          await permissionControl.requestCalendarPermission();
        }
      });
      return null;
    }, ["one-time"]);
    
    return CobbleScaffold.tab(
      title: tr.calendar.title,
      child: ListView(
        children: [
          CobbleTile.setting(
            leading: RebbleIcons.calendar,
            title: tr.calendar.toggleTitle,
            subtitle: tr.calendar.toggleSubtitle,
            child: Switch(
              value: calendarSyncEnabled.data?.value ?? false,
              onChanged: (value) async {
                await preferences.data?.value.setCalendarSyncEnabled(value);

                if (!value) {
                  calendarControl.deleteCalendarPinsFromWatch();
                }
              },
            ),
          ),
          CobbleDivider(),
          if (calendarSyncEnabled.data?.value ?? false) ...[
            CobbleTile.title(
              title: tr.calendar.choose,
            ),
            ...calendars.data?.value.map((e) {
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
                    calendarControl.requestCalendarSync();
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
