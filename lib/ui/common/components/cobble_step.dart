import 'package:flutter/material.dart';

import 'cobble_circle.dart';

class CobbleStep extends StatelessWidget {

  final String title;
  final Widget? child;
  final Widget icon;
  final Color? iconBackgroundColor;
  final EdgeInsets? iconPadding;

  const CobbleStep({Key? key, required this.icon, required this.title, this.child, this.iconBackgroundColor, this.iconPadding = const EdgeInsets.all(20)}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(top: MediaQuery.of(context).size.height / 8, left: 8, right: 8),
      child: Column(
        children: <Widget>[
          CobbleCircle(
            child: icon,
            diameter: 120,
            color: iconBackgroundColor ?? Theme.of(context).primaryColor,
            padding: iconPadding,
          ),
          const SizedBox(height: 16.0), // spacer
          Container(
            margin: const EdgeInsets.symmetric(vertical: 16),
            child: Text(
              title,
              style: Theme.of(context).textTheme.headlineMedium,
              textAlign: TextAlign.center,
            ),
          ),
          if (child != null) child!,
        ],
      ),
    );
  }

}