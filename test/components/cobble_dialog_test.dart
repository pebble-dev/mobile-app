import 'package:cobble/ui/common/components/cobble_dialog.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/cupertino.dart';

import 'flutter_test_config.dart';

Widget dialogs() => Column(
      children: [
        CobbleDialog(
          title: 'Title of dialog',
          content:
              'Dialog body text, this can go into detail or provide context for the action that is being asked about',
          negative: 'Cancel',
          positive: 'Confirm',
        ),
        CobbleDialog(
          title: 'Backup created successfully!',
          positive: 'Ok',
        ),
        Builder(
          builder: (context) => CobbleDialog(
            title: 'Permanently delete ALL health data?',
            content: 'This cannot be undone!',
            negative: 'Cancel',
            positive: 'Delete',
            intent: context.scheme!.destructive,
          ),
        ),
      ],
    );

void main() {
  testUi('Cobble dialog', dialogs());
}
