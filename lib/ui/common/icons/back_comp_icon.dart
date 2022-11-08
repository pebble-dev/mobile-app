import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';

// This widget returns a compicon with additional background behind it
class BackCompIcon extends StatelessWidget {
  BackCompIcon(
      this.compIcon,
      { this.color,
        this.size = 48.0, }
      );
  final CompIcon compIcon;
  final Color? color;

  final double size;

  @override
  Widget build(BuildContext context) {
    return Container(
      child: Center(
        child: compIcon,
      ),
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: color ?? context.scheme!.primary,
        shape: BoxShape.circle),
    );
  }

}
