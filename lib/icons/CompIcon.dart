import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:fossil/icons/rebble_icons_stroke_only_icons.dart';

class CompIcon extends StatelessWidget {
  CompIcon(this.stroke, this.fill);
  final IconData stroke;
  final IconData fill;
  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        Icon(stroke, color: Colors.black,),
        Icon(fill, color: Colors.white,)
      ],
    );
  }

}