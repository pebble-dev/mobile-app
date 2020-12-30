import 'package:flutter/material.dart';

/// Simple wrapper around Material FAB. Prefer to use this widget instead of
/// [FloatingActionButton.extended]
class CobbleFab extends StatelessWidget {
  final VoidCallback onPressed;
  final String label;
  final IconData icon;

  /// You will probably never need to use this, use only if you wish to
  /// display multiple FABs on same page. Can be anything, also simple string.
  final Object heroTag;

  const CobbleFab({
    Key key,
    @required this.onPressed,
    @required this.label,
    this.icon,
    this.heroTag,
  })  : assert(
          label is String && label.length > 0,
          "CobbleFab needs label",
        ),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton.extended(
      onPressed: null,
      icon: icon is IconData ? Icon(icon, size: 21) : null,
      label: label is String ? Text(label.toUpperCase()) : null,
      heroTag: heroTag,
    );
  }
}
