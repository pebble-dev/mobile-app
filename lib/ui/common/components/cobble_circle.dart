import 'package:flutter/material.dart';

class CobbleCircle extends StatelessWidget {
  const CobbleCircle(
      {Key? key,
        this.child,
        this.diameter,
        this.color,
        this.margin,
        this.padding,
        this.clip = false,
      }) : super(key: key);

  final Widget? child;
  final double? diameter;
  final Color? color;
  final EdgeInsets? margin;
  final EdgeInsets? padding;
  final bool clip;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
          color: color ?? Theme.of(context).dividerColor,
          shape: BoxShape.circle),
      child: Center(child: clip ? ClipOval(child: child) : child),
      width: diameter,
      height: diameter,
      margin: margin ?? EdgeInsets.zero,
      padding: padding ?? EdgeInsets.zero,
    );
  }
}
