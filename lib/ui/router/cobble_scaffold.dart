import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

import '../common/icons/fonts/rebble_icons.dart';

class CobbleScaffold extends StatelessWidget {
  final Widget child;
  final String? title;
  final String? subtitle;
  final List<Widget> actions;
  final FloatingActionButton? floatingActionButton;
  final FloatingActionButtonLocation? floatingActionButtonLocation;
  final Widget? bottomNavigationBar;
  final PreferredSizeWidget? bottomAppBar;

  const CobbleScaffold._({
    Key? key,
    required this.child,
    this.title,
    this.subtitle,
    this.actions = const [],
    this.floatingActionButton,
    this.floatingActionButtonLocation,
    this.bottomNavigationBar,
    this.bottomAppBar,
  })  : assert(title == null || title.length > 0),
        assert(subtitle == null ||
            (subtitle.length > 0 && title != null && title.length > 0)),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    Widget? navBarTitle;
    if (subtitle != null) {
      navBarTitle = _withSubtitle(context);
    } else if (title != null) {
      navBarTitle = _titleOnly(context);
    }

    Widget? leading;
    final route = ModalRoute.of(context);
    final bool canPop = route?.canPop ?? false;
    final bool useCloseButton = route is PageRoute && route.fullscreenDialog;
    if (canPop)
      leading = useCloseButton
          ? IconButton(
              icon: Icon(RebbleIcons.x_close),
              onPressed: () => Navigator.maybePop(context),
              tooltip: MaterialLocalizations.of(context).closeButtonTooltip,
            )
          : IconButton(
              icon: Icon(RebbleIcons.caret_left),
              onPressed: () => Navigator.maybePop(context),
              tooltip: MaterialLocalizations.of(context).backButtonTooltip,
            );

    final bottomHeight = bottomAppBar?.preferredSize.height ?? 0;
    final height = 25.0 + 16 * 2 + bottomHeight;

    return Scaffold(
      appBar: navBarTitle == null
          ? null
          : PreferredSize(
              preferredSize: Size.fromHeight(height),
              child: AppBar(
                leading: leading,
                title: navBarTitle,
                actions: actions,
                bottom: bottomAppBar,
              ),
            ),
      floatingActionButton: floatingActionButton,
      floatingActionButtonLocation: floatingActionButtonLocation,
      bottomNavigationBar: bottomNavigationBar,
      body: SafeArea(
        child: child,
      ),
    );
  }

  Column _withSubtitle(BuildContext context) => Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _titleOnly(context),
          SizedBox(height: 4),
          Text(
            subtitle!,
            style: context.theme.appBarTheme.textTheme!.headline6!.copyWith(
              fontSize: 14,
              color: context.scheme!.muted,
            ),
          )
        ],
      );

  Text _titleOnly(BuildContext context) => Text(
        title!,
      );

  /// Implements the basic material design visual layout structure.
  ///
  /// You should use [CobbleScaffold.page] when screen is displayed outside of
  /// tab view.
  ///
  /// See also:
  ///
  ///  * [CobbleScaffold.tab], which should be used when screen is displayed inside of
  ///  tab view.
  ///  * [CobbleSheet], API to display modal or inline bottom sheet
  static Widget page({
    Key? key,
    required Widget child,
    String? title,
    String? subtitle,
    List<Widget> actions = const [],
    FloatingActionButton? floatingActionButton,
    FloatingActionButtonLocation? floatingActionButtonLocation,
    Widget? bottomNavigationBar,
  }) =>
      CobbleScaffold._(
        key: key,
        child: child,
        title: title,
        subtitle: subtitle,
        floatingActionButton: floatingActionButton,
        floatingActionButtonLocation: floatingActionButtonLocation,
        actions: actions,
        bottomNavigationBar: bottomNavigationBar,
      );

  /// Implements the basic material design visual layout structure.
  ///
  /// You should use [CobbleScaffold.tab] when screen is displayed inside of
  /// tab view.
  ///
  /// See also:
  ///
  ///  * [CobbleScaffold.page], which should be used when screen is displayed outside of
  ///  tab view.
  ///  * [CobbleSheet], API to display modal or inline bottom sheet
  static Widget tab({
    Key? key,
    required Widget child,
    String? title,
    String? subtitle,
    List<Widget> actions = const [],
    FloatingActionButton? floatingActionButton,
    FloatingActionButtonLocation? floatingActionButtonLocation,
    PreferredSizeWidget? bottomAppBar,
  }) =>
      EnsureTabScaffold(
        child: CobbleScaffold._(
          key: key,
          child: child,
          title: title,
          subtitle: subtitle,
          floatingActionButton: floatingActionButton,
          floatingActionButtonLocation: floatingActionButtonLocation,
          actions: actions,
          bottomAppBar: bottomAppBar,
        ),
      );
}

/// Ensures widget is inside CobbleScaffold.tab, used primarily by
/// [InlineCobbleSheet.show] to scope bottom sheet inside tab
class EnsureTabScaffold extends InheritedWidget {
  const EnsureTabScaffold({
    Key? key,
    required Widget child,
  })   : super(key: key, child: child);

  @override
  bool updateShouldNotify(covariant InheritedWidget oldWidget) => false;
}
