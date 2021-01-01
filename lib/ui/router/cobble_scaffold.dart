import 'package:cobble/ui/common/icons/fonts/rebble_icons_stroke.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

class CobbleScaffold extends StatelessWidget {
  final Widget child;
  final String title;
  final String subtitle;
  final FloatingActionButton floatingActionButton;
  final FloatingActionButtonLocation floatingActionButtonLocation;

  const CobbleScaffold({
    Key key,
    @required this.child,
    this.title,
    this.subtitle,
    this.floatingActionButton,
    this.floatingActionButtonLocation,
  })  : assert(child != null),
        assert(title == null || title.length > 0),
        assert(subtitle == null ||
            (subtitle.length > 0 && title != null && title.length > 0)),
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
    final parentRoute = ModalRoute.of(context);
    final bool canPop = parentRoute?.canPop ?? false;
    final bool useCloseButton =
        parentRoute is PageRoute && parentRoute.fullscreenDialog;
    if (canPop)
      leading = useCloseButton
          ? IconButton(
              icon: Icon(RebbleIconsStroke.x_close),
              onPressed: () => Navigator.maybePop(context),
              tooltip: MaterialLocalizations.of(context).closeButtonTooltip,
            )
          : IconButton(
              icon: Icon(RebbleIconsStroke.caret_left),
              onPressed: () => Navigator.maybePop(context),
              tooltip: MaterialLocalizations.of(context).backButtonTooltip,
            );

    return Scaffold(
      appBar: navBarTitle == null
          ? null
          : PreferredSize(
              preferredSize: Size.fromHeight(56),
              child: AppBar(
                leading: leading,
                title: navBarTitle,
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
