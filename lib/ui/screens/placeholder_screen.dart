import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';

class PlaceholderScreen extends StatelessWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold.tab(
      title: 'Placeholder screen',
      child: Placeholder(),
    );
  }
}
