import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

/// Shows a modal Cobble design bottom sheet.
///
/// A modal bottom sheet prevents the user from interacting with the rest of
/// the app until she dismisses it.
///
/// The [context] argument is used to look up the [Navigator] and [Theme] for
/// the bottom sheet. It is only used when the method is called. Its
/// corresponding widget can be safely removed from the tree before the bottom
/// sheet is closed. Inside [builder] function you should use [context] provided
/// to builder instead of one passed to [showCobbleSheet].
Future<void> showCobbleSheet({
  @required BuildContext context,
  @required Widget Function(BuildContext context, VoidCallback close) builder,
  bool dismissible = true,
}) async {
  final borderRadius = BorderRadius.vertical(
    top: Radius.circular(4),
    bottom: Radius.zero,
  );
  await showModalBottomSheet(
    context: context,
    useRootNavigator: true,
    shape: RoundedRectangleBorder(
      borderRadius: borderRadius,
    ),
    isScrollControlled: true,
    builder: (context) {
      final close = () => Navigator.of(context).pop();
      return Container(
        decoration: BoxDecoration(
          color: context.scheme.elevated,
          borderRadius: borderRadius,
        ),
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: EdgeInsets.only(top: 8),
              child: Row(
                children: [
                  Flexible(
                    flex: 132,
                    child: Container(),
                  ),
                  Flexible(
                    flex: 96,
                    child: Container(
                      height: 4,
                      decoration: BoxDecoration(
                        color: context.scheme.divider,
                        borderRadius: BorderRadius.all(
                          Radius.circular(2),
                        ),
                      ),
                    ),
                  ),
                  Flexible(
                    flex: 132,
                    child: Container(),
                  ),
                ],
              ),
            ),
            builder(context, close),
          ],
        ),
      );
    },
  );
}
