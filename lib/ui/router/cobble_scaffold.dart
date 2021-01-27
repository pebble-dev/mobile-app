import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

import '../common/icons/fonts/rebble_icons.dart';

class CobbleScaffold extends StatelessWidget {
  final Widget child;
  final String title;
  final String subtitle;
  final List<Widget> actions;
  final FloatingActionButton floatingActionButton;
  final FloatingActionButtonLocation floatingActionButtonLocation;

  const CobbleScaffold({
    Key key,
    @required this.child,
    this.title,
    this.subtitle,
    this.floatingActionButton,
    this.floatingActionButtonLocation,
    this.actions = const [],
  })  : assert(child != null),
        assert(title == null || title.length > 0),
        assert(subtitle == null ||
            (subtitle.length > 0 && title != null && title.length > 0)),
        assert(actions != null),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    Widget navBarTitle;
    if (subtitle != null) {
      navBarTitle = _withSubtitle(context);
    } else if (title != null) {
      navBarTitle = _titleOnly(context);
    }

    Widget leading;
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

    final height = 25.0 + 16 * 2;

    return Scaffold(
      appBar: navBarTitle == null
          ? null
          : PreferredSize(
              preferredSize: Size.fromHeight(height),
              child: AppBar(
                leading: leading,
                title: navBarTitle,
                actions: actions,
              ),
            ),
      floatingActionButton: floatingActionButton,
      floatingActionButtonLocation: floatingActionButtonLocation,
      body: child,
    );
  }

  Column _withSubtitle(BuildContext context) => Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _titleOnly(context),
          SizedBox(height: 4),
          Text(
            subtitle,
            style: context.theme.appBarTheme.textTheme.headline6.copyWith(
              fontSize: 14,
              color: context.scheme.muted,
            ),
          )
        ],
      );

  Text _titleOnly(BuildContext context) => Text(
        title,
      );
}
