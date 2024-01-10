import 'package:cobble/domain/apps/app_logs.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class TestLogsPage extends HookConsumerWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final logs = ref.watch(recievedLogsProvider);

    return ListView.builder(
        itemBuilder: (context, index) {
          final entry = logs[index];
          return Text(
              "[${entry.filename}:${entry.lineNumber}] ${entry.message}");
        },
        itemCount: logs.length);
  }
}
