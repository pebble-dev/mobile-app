import 'package:cobble/ui/common/icons/fonts/rebble_icons_stroke.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

class CobbleTile extends StatelessWidget {
  final EdgeInsets padding;
  final bool grayscale;
  final IconData leading;
  final Widget trailing;
  final String title;
  final String subtitle;
  final String body;
  final void Function() onTap;
  final CobbleScreen navigateTo;
  final Color intent;

  /// Prefer using [CobbleTile.navigation] or [CobbleTile.setting] constructors.
  @protected
  const CobbleTile._({
    Key key,
    this.padding = const EdgeInsets.all(0),
    this.grayscale = false,
    this.leading,
    this.trailing,
    @required this.title,
    this.subtitle,
    this.body,
    this.onTap,
    this.navigateTo,
    this.intent,
  })  : assert(title != null && title.length > 0),
        assert(
          onTap == null || navigateTo == null,
          'You can use only one of onTap and navigateTo, not both',
        ),
        assert(
          subtitle == null || body == null,
          'You can use only one of subtitle or body, not both',
        ),
        assert(
          intent == null || grayscale == false,
          'You can use only one of intent or grayscale, not both',
        ),
        super(key: key);

  /// Simple tile that displays [title] and optional [body], usually used as
  /// title of ListView
  factory CobbleTile.title({
    Key key,
    @required String title,
    String body,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.fromLTRB(16, 24, 16, 8),
        title: title,
        body: body,
      );

  /// Specialised to provider bigger tap area, that navigates user to another
  /// screen. It includes [leading] and [trailing] icons and can be colored
  /// with [intent].
  factory CobbleTile.navigation({
    Key key,
    IconData leading,
    IconData trailing = RebbleIconsStroke.caret_right,
    @required String title,
    String subtitle,
    @required CobbleScreen navigateTo,
    Color intent,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(16),
        leading: leading,
        trailing: Icon(
          trailing,
          size: 25,
        ),
        title: title,
        subtitle: subtitle,
        navigateTo: navigateTo,
        intent: intent,
      );

  /// Specialised to provider bigger tap area, with [leading] and [trailing]
  /// icons. Can be colored with [intent].
  factory CobbleTile.action({
    Key key,
    IconData leading,
    IconData trailing,
    @required String title,
    String subtitle,
    @required void Function() onTap,
    Color intent,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(16),
        leading: leading,
        trailing: trailing != null
            ? Icon(
                trailing,
                size: 25,
              )
            : null,
        title: title,
        subtitle: subtitle,
        onTap: onTap,
        intent: intent,
      );

  /// Specialised to show simple interactive [trailing] widget, usually used to
  /// toggle some setting
  factory CobbleTile.setting({
    Key key,
    IconData leading,
    @required String title,
    String subtitle,
    @required Widget child,
    Color intent,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(16),
        leading: leading,
        title: title,
        subtitle: subtitle,
        trailing: child,
        intent: intent,
      );

  /// [CobbleTile.info] doesn't include padding as it's not meant to be used
  /// in ListView but in [CobbleCard]
  factory CobbleTile.info({
    Key key,
    IconData leading,
    @required String title,
    String subtitle,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(0),
        leading: leading,
        title: title,
        subtitle: subtitle,
        grayscale: true,
      );

  @override
  Widget build(BuildContext context) {
    final minHeight = 64.0;

    Widget child = Builder(
      builder: (context) => Container(
        padding: padding,
        constraints: BoxConstraints(
          minHeight: minHeight,
        ),
        child: Row(
          children: [
            if (leading != null) ...[
              Icon(
                leading,
                size: 25,
              ),
              SizedBox(width: 16),
            ],
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: context.textTheme.headline6,
                  ),
                  if (body is String) ...[
                    SizedBox(height: 4),
                    Text(
                      body,
                      style: context.textTheme.bodyText2,
                    ),
                  ],
                  if (subtitle is String) ...[
                    SizedBox(height: 4),
                    Text(
                      subtitle,
                      style: context.textTheme.bodyText2.copyWith(
                        color: context.textTheme.bodyText2.color.withOpacity(
                          context.scheme.muted.opacity,
                        ),
                      ),
                    ),
                  ]
                ],
              ),
            ),
            if (trailing != null) ...[
              Container(
                constraints: BoxConstraints(
                  maxHeight: minHeight - padding.vertical,
                ),
                child: trailing,
              ),
            ],
          ],
        ),
      ),
    );
    if (onTap != null)
      child = InkWell(
        onTap: onTap,
        child: child,
      );
    if (navigateTo != null)
      child = InkWell(
        onTap: _navigate(context),
        child: child,
      );
    if (grayscale)
      child = Theme(
        data: context.theme.copyWith(
          iconTheme: context.theme.iconTheme.copyWith(
            color: context.scheme.muted,
          ),
        ),
        child: child,
      );
    if (intent != null)
      child = Theme(
        data: context.theme.copyWith(
          iconTheme: context.theme.iconTheme.copyWith(
            color: intent,
          ),
          textTheme: context.textTheme.copyWith(
            bodyText2: context.textTheme.bodyText2.copyWith(
              color: intent,
            ),
            headline6: context.textTheme.headline6.copyWith(
              color: intent,
            ),
          ),
        ),
        child: child,
      );
    return child;
  }

  void Function() _navigate(BuildContext context) =>
      () => context.push(navigateTo);
}
