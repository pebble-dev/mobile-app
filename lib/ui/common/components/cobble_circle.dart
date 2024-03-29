import 'package:flutter/material.dart';

class CobbleCircle extends StatelessWidget {
  CobbleCircle(
      {this.child, this.diameter, this.color, this.margin, this.padding});

  final Widget? child;
  final double? diameter;
  final Color? color;
  final EdgeInsets? margin;
  final EdgeInsets? padding;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
          color: color == null ? Theme.of(context).dividerColor : color,
          shape: BoxShape.circle),
      child: Center(child: ClipOval(child: child)),
      width: diameter,
      height: diameter,
      margin: margin == null ? EdgeInsets.zero : margin,
      padding: padding == null ? EdgeInsets.zero : padding,
    );
  }
}
