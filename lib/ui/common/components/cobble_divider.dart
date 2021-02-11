import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

/// Divider you can use to visually separate children in ListView
class CobbleDivider extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      height: 2,
      decoration: BoxDecoration(
        color: context.scheme.divider,
      ),
    );
  }
}
