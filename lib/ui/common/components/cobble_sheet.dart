import 'dart:async';

import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';

class CobbleSheet {
  /// Shows a modal Cobble design bottom sheet.
  ///
  /// A modal bottom sheet prevents the user from interacting with the rest of
  /// the app until she dismisses it.
  ///
  /// The [context] argument is used to look up the [Navigator] and [Theme] for
  /// the bottom sheet. It is only used when the method is called. Its
  /// corresponding widget can be safely removed from the tree before the bottom
  /// sheet is closed. Inside [builder] function you should use [context] provided
  /// to builder instead of one passed to [CobbleSheet.showModal].
  static Future<void> showModal({
    @required BuildContext context,
    @required Widget Function(BuildContext context) builder,
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
      builder: (context) {
        return Container(
          decoration: BoxDecoration(
            color: context.scheme.elevated,
            borderRadius: borderRadius,
          ),
          padding: EdgeInsets.only(
            bottom: MediaQuery.of(context).viewInsets.bottom,
          ),
          child: ConstrainedBox(
            constraints: BoxConstraints(
              maxHeight: MediaQuery.of(context).size.height / 3,
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Padding(
                  padding: EdgeInsets.symmetric(vertical: 8),
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
                Flexible(
                  child: SingleChildScrollView(
                    child: builder(context),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  /// Creates [InlineCobbleSheet] controller and returns it.
  ///
  /// Example:
  /// ```dart
  /// @override
  /// Widget build(BuildContext context) {
  ///   final sheet = CobbleSheet.useInline();
  ///   Button(
  ///     onPressed: () {
  ///       sheet.show(
  ///         context: context,
  ///         builder: (context) {
  ///           return Button(
  ///             onPressed: sheet.close,
  ///           );
  ///         },
  ///       );
  ///     },
  ///   );
  /// }
  /// ```
  static InlineCobbleSheet useInline() {
    final _controller = useState<PersistentBottomSheetController<void>>(null);
    final _sheet = useMemoized(() => InlineCobbleSheet(_controller));
    return _sheet;
  }
}

/// Controller for a Cobble design bottom sheet that is displayed inline in the
/// nearest [Scaffold] ancestor.
///
/// You shouldn't use this class directly, use [CobbleSheet.useInline] hook
/// instead.
///
/// See also:
/// * [CobbleSheet.useInline]
/// * [InlineCobbleSheet.show]
class InlineCobbleSheet {
  final ValueNotifier<PersistentBottomSheetController<void>> _controller;

  InlineCobbleSheet(this._controller);

  set _ctr(PersistentBottomSheetController<void> controller) {
    _controller.value = controller;
    if (controller != null) {
      _shown = true;
      controller.closed.then((value) {
        _shown = false;
        _controller.value = null;
      });
    }
  }

  bool _shown = false;

  bool get shown {
    return _controller.value != null && _shown;
  }

  /// Shows a Cobble design inline bottom sheet in the nearest [Scaffold] ancestor.
  ///
  /// The [context] argument is used to look up the [Scaffold] for
  /// the bottom sheet. It is only used when the method is called. Its
  /// corresponding widget can be safely removed from the tree before the bottom
  /// sheet is closed. Inside [builder] function you should use [context] provided
  /// to builder instead of one passed to [InlineCobbleSheet.show].
  ///
  /// Bottom sheet will be closed automatically if user clicks on back button
  /// in app bar.
  ///
  /// NOTE: Make sure [context] you've provided is inside [CobbleScaffold.tab],
  /// otherwise inline bottom sheet will escape current tab and be displayed
  /// above all tabs. This is probably not what you want.
  ///
  /// INCORRECT:
  /// ```dart
  /// @override
  /// Widget build(BuildContext context) {
  ///   final sheet = CobbleSheet.useInline();
  ///   return CobbleScaffold.tab(
  ///     actions: [
  ///       Button(
  ///         onPressed: () {
  ///           sheet.show(
  ///             context: context,
  ///             builder: (context) => Container(),
  ///           );
  ///         },
  ///       ),
  ///     ],
  ///   );
  /// ```
  /// CORRECT:
  /// ```dart
  /// @override
  /// Widget build(BuildContext context1) {
  ///   final sheet = CobbleSheet.useInline();
  ///   return CobbleScaffold.tab(
  ///     actions: [
  ///       Builder(
  ///         // Builder will provide new context that is inside tabbed CobbleScaffold
  ///         builder: (context2) => Button(
  ///           onPressed: () {
  ///             sheet.show(
  ///               context: context2,
  ///               builder: (context3) => Container(),
  ///             );
  ///           },
  ///         ),
  ///       ),
  ///     ],
  ///   );
  /// ```
  void show({
    @required BuildContext context,
    @required Widget Function(BuildContext context) builder,
  }) {
    if (shown) return;

    assert(
      context.dependOnInheritedWidgetOfExactType<EnsureTabScaffold>() != null,
      "BuildContext you've provided doesn't include CobbleScaffold.tab. "
      "See InlineCobbleSheet.show for more info",
    );

    final borderRadius = BorderRadius.vertical(
      top: Radius.circular(4),
      bottom: Radius.zero,
    );

    _ctr = showBottomSheet(
      context: context,
      shape: RoundedRectangleBorder(
        borderRadius: borderRadius,
      ),
      builder: (context) => Container(
        decoration: BoxDecoration(
          color: context.scheme.elevated,
          borderRadius: borderRadius,
        ),
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: ConstrainedBox(
          constraints: BoxConstraints(
            maxHeight: MediaQuery.of(context).size.height / 3,
          ),
          child: SingleChildScrollView(
            child: builder(context),
          ),
        ),
      ),
    );
  }

  /// Hides inline bottom sheet. Will be closed automatically if user clicks
  /// on back button in app bar.
  void close() {
    if (!shown) return;
    _controller.value.close();
  }
}
