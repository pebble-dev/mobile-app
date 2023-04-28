import 'package:flutter/material.dart';

/// Simple wrapper around Material FAB. Prefer to use this widget instead of
/// [FloatingActionButton.extended]
class CobbleFab extends FloatingActionButton {
  final VoidCallback onPressed;
  final String label;
  final IconData? icon;

  /// You will probably never need to use this, use only if you wish to
  /// display multiple FABs on same page. Can be anything, also simple string.
  final Object? heroTag;

  const CobbleFab({
    Key? key,
    required this.onPressed,
    required this.label,
    this.icon,
    this.heroTag,
  })  : assert(
          label is String && label.length > 0,
          "CobbleFab needs label",
        ),
        super(key: key, onPressed: onPressed);

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton.extended(
      onPressed: onPressed,
      icon: icon is IconData ? Icon(icon) : null,
      label: Text(label.toUpperCase()),
      heroTag: heroTag,
    );
  }
}
