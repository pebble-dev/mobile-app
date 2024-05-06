import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

// This widget returns an icon with both fill and stroke by layering a Stroke
// and Fill version of one icon on top of each other.
class CompIcon extends StatelessWidget {
  const CompIcon(
      this.stroke, this.fill,
      {Key? key,  this.strokeColor = Colors.black,
        this.fillColor = Colors.white,
        this.size, }
      ) : super(key: key);
  final IconData stroke;
  final IconData fill;
  final Color strokeColor;
  final Color fillColor;

  final double? size;

  @override
  Widget build(BuildContext context) {
    return Stack(
      alignment: Alignment.center,
      children: <Widget>[
        Icon(fill, color: fillColor, size: size,), // Draws underneath
        Icon(stroke, color: strokeColor, size: size,), // Draws on top
      ],
    );
  }

}