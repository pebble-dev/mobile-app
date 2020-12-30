import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/components/cobble_card.dart';
import 'package:cobble/ui/common/components/cobble_dialog.dart';
import 'package:cobble/ui/common/components/cobble_fab.dart';
import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons_stroke.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/health.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';

class WidgetLibrary extends StatelessWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
      title: 'Widget library',
      child: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Table(
                columnWidths: {
                  0: FlexColumnWidth(1),
                  1: FlexColumnWidth(2),
                  2: FlexColumnWidth(2),
                },
                children: [
                  TableRow(
                    children: [
                      Container(),
                      Text('Normal buttons'),
                      Text('Disabled buttons'),
                    ],
                  ),
                  TableRow(
                    children: [
                      Text('Outlined\nbuttons'),
                      _row(
                        children: [
                          CobbleButton(
                            onPressed: () {},
                            label: 'Button',
                          ),
                          CobbleButton(
                            onPressed: () {},
                            icon: RebbleIconsStroke.notifications_megaphone,
                            label: 'Button',
                          ),
                          CobbleButton(
                            onPressed: () {},
                            icon: RebbleIconsStroke.notifications_megaphone,
                          ),
                        ],
                      ),
                      _row(
                        children: [
                          CobbleButton(
                            onPressed: null,
                            label: 'Button',
                          ),
                          CobbleButton(
                            onPressed: null,
                            icon: RebbleIconsStroke.notifications_megaphone,
                            label: 'Button',
                          ),
                          CobbleButton(
                            onPressed: null,
                            icon: RebbleIconsStroke.notifications_megaphone,
                          ),
                        ],
                      ),
                    ],
                  ),
                  TableRow(
                    children: [
                      Text('Flat\nbuttons'),
                      _row(
                        children: [
                          CobbleButton(
                            outlined: false,
                            onPressed: () {},
                            label: 'Button',
                          ),
                          CobbleButton(
                            outlined: false,
                            onPressed: () {},
                            icon: RebbleIconsStroke.notifications_megaphone,
                            label: 'Button',
                          ),
                          CobbleButton(
                            outlined: false,
                            onPressed: () {},
                            icon: RebbleIconsStroke.notifications_megaphone,
                          ),
                        ],
                      ),
                      _row(
                        children: [
                          CobbleButton(
                            outlined: false,
                            onPressed: null,
                            label: 'Button',
                          ),
                          CobbleButton(
                            outlined: false,
                            onPressed: null,
                            icon: RebbleIconsStroke.notifications_megaphone,
                            label: 'Button',
                          ),
                          CobbleButton(
                            outlined: false,
                            onPressed: null,
                            icon: RebbleIconsStroke.notifications_megaphone,
                          ),
                        ],
                      ),
                    ],
                  ),
                ],
              ),
              Text('Floating action buttons (FAB)'),
              _row(
                children: [
                  CobbleFab(
                    onPressed: () {},
                    label: 'LABEL',
                    // Don't use on your code
                    heroTag: '1',
                  ),
                  CobbleFab(
                    onPressed: () {},
                    icon: RebbleIconsStroke.notifications_megaphone,
                    label: 'LABEL',
                    // Don't use on your code
                    heroTag: '2',
                  ),
                ],
              ),
              Text('Form elements'),
              _row(
                children: [
                  Switch(
                    value: true,
                    onChanged: (bool value) {},
                  ),
                  Switch(
                    value: false,
                    onChanged: (bool value) {},
                  ),
                  Checkbox(
                    value: true,
                    onChanged: (bool value) {},
                  ),
                  Checkbox(
                    value: false,
                    onChanged: (bool value) {},
                  ),
                ],
              ),
              Text('Dialogs'),
              _row(
                children: [
                  CobbleButton(
                    label: 'Open dialog',
                    onPressed: () async {
                      // Will return true if positive action is tapped and false if negative action is tapped or is dismissed
                      final result = await showCobbleDialog(
                        context: context,
                        title: 'Title of dialog',
                        content:
                            'Dialog body text, this can go into detail or provide context for the action that is being asked about',
                        negative: 'Cancel',
                        positive: 'Confirm',
                      );
                      print(result);
                    },
                  ),
                  CobbleButton(
                    label: 'Do backup',
                    onPressed: () {
                      // Can be opened without waiting for result
                      showCobbleDialog(
                        context: context,
                        title: 'Backup created successfully!',
                        positive: 'Ok',
                      );
                    },
                  ),
                  CobbleButton(
                    label: 'Delete health data',
                    onPressed: () {
                      showCobbleDialog(
                        context: context,
                        dismissible: false,
                        title: 'Permanently delete ALL health data?',
                        content: 'This cannot be undone!',
                        negative: 'Cancel',
                        positive: 'Delete',
                        // Can be styled differently with intents
                        intent: context.scheme.destructive,
                      );
                    },
                  ),
                ],
              ),
              Text('Cards'),
              CobbleCard(
                title: 'Rebble account',
                subtitle: 'support@rebble.io',
                leading: Svg('images/app_icon.svg'),
                child: Column(
                  children: [
                    CobbleTile.info(
                      leading: RebbleIconsStroke.dictation_microphone,
                      title: 'Voice and weather subscription',
                      subtitle: 'Next charge 6/9/20',
                    ),
                    CobbleTile.info(
                      leading: RebbleIconsStroke.timeline_pin,
                      title: 'Timeline sync',
                      subtitle: 'Every 30 minutes',
                    ),
                  ],
                ),
                actions: [
                  CobbleCardAction(
                    onPressed: () {},
                    label: 'Sign out',
                  ),
                  CobbleCardAction(
                    onPressed: () {},
                    label: 'Manage account',
                  ),
                ],
              ),
              CobbleCard(
                title: 'Untrusted boot URL',
                leading: RebbleIconsStroke.notifications,
                intent: context.scheme.danger,
                actions: [
                  CobbleCardAction(
                    onPressed: () {},
                    label: 'Reset',
                  ),
                  CobbleCardAction(
                    onPressed: () {},
                    label: 'Copy url',
                  ),
                ],
              ),
              Text('Bottom sheet'),
              _row(
                children: [
                  CobbleButton(
                    label: 'Open bottom sheet',
                    onPressed: () {
                      showCobbleSheet(
                        context: context,
                        builder: (context) => Column(
                          children: [
                            CobbleTile.info(
                              leading: RebbleIconsStroke.dictation_microphone,
                              title: 'Voice and weather subscription',
                              subtitle: 'Next charge 6/9/20',
                            ),
                            CobbleTile.info(
                              leading: RebbleIconsStroke.timeline_pin,
                              title: 'Timeline sync',
                              subtitle: 'Every 30 minutes',
                            ),
                          ],
                        ),
                      );
                    },
                  ),
                ],
              ),
              Text('Bottom sheet'),
              _row(children: [
                ListView(
                  shrinkWrap: true,
                  children: [
                    CobbleTile.title(
                      title: 'Rebble Health',
                      body:
                          'Supported watches can keep track of your fitness data for '
                          'you, including steps, sleep, and heart rate',
                    ),
                    CobbleTile.setting(
                      leading: RebbleIconsStroke.health,
                      title: 'Track my health data',
                      child: Switch(
                        value: true,
                        onChanged: (bool value) {},
                      ),
                    ),
                    CobbleTile.navigation(
                      leading: RebbleIconsStroke.health,
                      trailing: RebbleIconsStroke.caret_right,
                      title: 'Manage health database',
                      navigateTo: Health(),
                    ),
                    CobbleTile.action(
                      leading: RebbleIconsStroke.health,
                      title: 'Permanently delete all health data',
                      intent: context.scheme.destructive,
                      onTap: () {},
                    ),
                  ],
                )
              ])
            ],
          ),
        ),
      ),
    );
  }

  Padding _row({List<Widget> children}) => Padding(
        padding: EdgeInsets.symmetric(vertical: 8),
        child: Wrap(
          children: children,
        ),
      );
}
