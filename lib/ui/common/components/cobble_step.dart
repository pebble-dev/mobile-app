import 'package:flutter/material.dart';

import 'cobble_circle.dart';

class CobbleStep extends StatelessWidget {

  final String title;
  final Widget? child;
  final Widget icon;

  const CobbleStep({Key? key, required this.icon, required this.title, this.child}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(top: MediaQuery.of(context).size.height / 8, left: 8, right: 8),
      child: Column(
        children: <Widget>[
          CobbleCircle(
            child: icon,
            diameter: 120,
            color: Theme.of(context).primaryColor,
            padding: const EdgeInsets.all(20),
          ),
          const SizedBox(height: 16.0), // spacer
          Container(
            margin: const EdgeInsets.symmetric(vertical: 8),
            child: Text(
              title,
              style: Theme.of(context).textTheme.headline4,
              textAlign: TextAlign.center,
            ),
          ),
          const SizedBox(height: 24.0), // spacer
          if (child != null) child!,
        ],
      ),
    );
  }

}