import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/components/cobble_card.dart';
import 'package:cobble/ui/common/components/cobble_dialog.dart';
import 'package:cobble/ui/common/components/cobble_fab.dart';
import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/health.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';

class WidgetLibrary extends HookWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    final inlineSheet = CobbleSheet.useInline();
    return CobbleScaffold.tab(
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
                            icon: RebbleIcons.notification,
                            label: 'Button',
                          ),
                          CobbleButton(
                            onPressed: () {},
                            icon: RebbleIcons.notification,
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
                            icon: RebbleIcons.notification,
                            label: 'Button',
                          ),
                          CobbleButton(
                            onPressed: null,
                            icon: RebbleIcons.notification,
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
                            icon: RebbleIcons.notification,
                            label: 'Button',
                          ),
                          CobbleButton(
                            outlined: false,
                            onPressed: () {},
                            icon: RebbleIcons.notification,
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
                            icon: RebbleIcons.notification,
                            label: 'Button',
                          ),
                          CobbleButton(
                            outlined: false,
                            onPressed: null,
                            icon: RebbleIcons.notification,
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
                    icon: RebbleIcons.notification,
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
                      leading: RebbleIcons.dictation_microphone,
                      title: 'Voice and weather subscription',
                      subtitle: 'Next charge 6/9/20',
                    ),
                    CobbleTile.info(
                      leading: RebbleIcons.timeline_pin,
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
                leading: RebbleIcons.notification,
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
                      CobbleSheet.showModal(
                        context: context,
                        builder: (context) => Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            children: [
                              CobbleTile.info(
                                leading: RebbleIcons.dictation_microphone,
                                title: 'Voice and weather subscription',
                                subtitle: 'Next charge 6/9/20',
                              ),
                              CobbleTile.info(
                                leading: RebbleIcons.timeline_pin,
                                title: 'Timeline sync',
                                subtitle: 'Every 30 minutes',
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                  Builder(
                    builder: (context) => CobbleButton(
                      label: 'Open inline sheet',
                      onPressed: () {
                        inlineSheet.show(
                          context: context,
                          builder: (context) => Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Padding(
                                padding: const EdgeInsets.all(16),
                                child: CobbleButton(
                                  label: 'Close inline sheet',
                                  onPressed: inlineSheet.close,
                                ),
                              ),
                            ],
                          ),
                        );
                      },
                    ),
                  ),
                ],
              ),
              Text('Cobble tiles'),
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
                      leading: RebbleIcons.health_heart,
                      title: 'Track my health data',
                      child: Switch(
                        value: true,
                        onChanged: (bool value) {},
                      ),
                    ),
                    CobbleTile.navigation(
                      leading: RebbleIcons.health_heart,
                      trailing: RebbleIcons.caret_right,
                      title: 'Manage health database',
                      navigateTo: Health(),
                    ),
                    CobbleTile.action(
                      leading: RebbleIcons.health_heart,
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
