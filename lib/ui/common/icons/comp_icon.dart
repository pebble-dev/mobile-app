import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

// This widget returns an icon with both fill and stroke by layering a Stroke
// and Fill version of one icon on top of each other.
class CompIcon extends StatelessWidget {
  CompIcon(
      this.stroke, this.fill,
      { this.strokeColor = Colors.black,
        this.fillColor = Colors.white,
        this.size = 25.0, }
      );
  final IconData stroke;
  final IconData fill;
  final Color strokeColor;
  final Color fillColor;

  final double size;

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        Icon(fill, color: fillColor,), // Draws underneath
        Icon(stroke, color: strokeColor,), // Draws on top
      ],
    );
  }

}