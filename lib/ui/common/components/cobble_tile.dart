import 'dart:math';

import 'package:cobble/ui/common/components/cobble_accordion.dart';
import 'package:cobble/ui/common/icons/back_comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

class CobbleTile extends StatelessWidget {
  final EdgeInsets padding;
  final bool grayscale;
  final Widget? leading;
  final Widget? trailing;
  final String title;
  final String? subtitle;
  final String? body;
  final void Function()? onTap;
  final CobbleScreen? navigateTo;
  final Color? intent;

  /// Prefer using named constructors instead of this one.
  @protected
  const CobbleTile._({
    Key? key,
    this.padding = const EdgeInsets.all(0),
    this.grayscale = false,
    this.leading,
    this.trailing,
    required this.title,
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
  /// main title of ListView
  factory CobbleTile.title({
    Key? key,
    required String title,
    String? body,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.fromLTRB(16, 24, 16, 8),
        title: title,
        body: body,
      );

  /// Simple tile that displays [title] and optional [body], usually used as
  /// title of subsection in ListView.
  ///
  /// If you wish to visually align tiles without using [leading], you can
  /// use [reservedIconSpace].
  factory CobbleTile.sectionTitle({
    Key? key,
    Object? leading,
    required String title,
    String? body,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(16),
        leading: _leadingToWidget(leading),
        title: title,
        subtitle: body,
      );

  /// Specialised to provide bigger tap area, that navigates user to another
  /// screen. It includes [leading] and [trailing] icons and can be colored
  /// with [intent].
  factory CobbleTile.navigation({
    Key? key,
    Object? leading,
    IconData trailing = RebbleIcons.caret_right,
    required String title,
    String? subtitle,
    required CobbleScreen navigateTo,
    Color? intent,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(16),
        leading: _leadingToWidget(leading),
        trailing: Icon(
          trailing,
        ),
        title: title,
        subtitle: subtitle,
        navigateTo: navigateTo,
        intent: intent,
      );

  /// Specialised to provide bigger tap area, with [leading] and [trailing]
  /// icons. Can be colored with [intent].
  factory CobbleTile.action({
    Key? key,
    Object? leading,
    IconData? trailing,
    required String title,
    String? subtitle,
    required void Function() onTap,
    Color? intent,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(16),
        leading: _leadingToWidget(leading),
        trailing: trailing != null
            ? Icon(
                trailing,
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
    Key? key,
    Object? leading,
    required String title,
    String? subtitle,
    required Widget child,
    Color? intent,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(16),
        leading: _leadingToWidget(leading),
        title: title,
        subtitle: subtitle,
        trailing: child,
        intent: intent,
      );

  /// [CobbleTile.info] doesn't include padding as it's not meant to be used
  /// in ListView but in [CobbleCard]
  factory CobbleTile.info({
    Key? key,
    Object? leading,
    required String title,
    String? subtitle,
  }) =>
      CobbleTile._(
        key: key,
        padding: EdgeInsets.all(0),
        leading: _leadingToWidget(leading),
        title: title,
        subtitle: subtitle,
        grayscale: true,
      );

  static CobbleAccordion accordion({
    Key? key,
    Object? leading,
    required String title,
    required List<CobbleTile> children,
  }) =>
      CobbleAccordion(
        headerBuilder: (onTap, heightFactor) => CobbleTile._(
          padding: EdgeInsets.all(16),
          title: title,
          onTap: onTap,
          leading: _leadingToWidget(leading),
          trailing: Transform.rotate(
            angle: heightFactor * pi,
            child: Icon(RebbleIcons.caret_down),
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: children,
        ),
      );

  factory CobbleTile.app({
    Key? key,
    ImageProvider? leading,
    required String title,
    String? subtitle,
    Widget? child,
    void Function()? onTap,
  }) =>
      CobbleTile._(
        key: key,
        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 12),
        leading: _leadingToWidget(leading, size: 48),
        title: title,
        subtitle: subtitle,
        trailing: child,
        onTap: onTap,
      );

  factory CobbleTile.appNavigation({
    Key? key,
    ImageProvider? leading,
    required String title,
    String? subtitle,
    IconData trailing = RebbleIcons.caret_right,
    required CobbleScreen navigateTo,
  }) =>
      CobbleTile._(
        key: key,
        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 12),
        leading: _leadingToWidget(leading, size: 48),
        title: title,
        subtitle: subtitle,
        trailing: Icon(trailing),
        navigateTo: navigateTo,
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
              leading!,
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
                      body!,
                      style: context.textTheme.bodyText2,
                    ),
                  ],
                  if (subtitle is String) ...[
                    SizedBox(height: 4),
                    Text(
                      subtitle!,
                      style: context.textTheme.bodyText2!.copyWith(
                        color: context.textTheme.bodyText2!.color!.withOpacity(
                          context.scheme!.muted.opacity,
                        ),
                      ),
                    ),
                  ],
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
            color: context.scheme!.muted,
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
            bodyText2: context.textTheme.bodyText2!.copyWith(
              color: intent,
            ),
            headline6: context.textTheme.headline6!.copyWith(
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

  /// Can be passed as [leading] argument instead of actual IconData to
  /// visually align titles of tiles without using icons.
  static const reservedIconSpace = Icons.bathtub;

  /// Wrap [CobbleTile] with this wrapper to provide color to icon.
  static Widget withIconColor({
    required Color color,
    required Widget child,
  }) {
    assert(color != null);
    assert(child != null);
    return Builder(
      builder: (context) => Theme(
        data: context.theme.copyWith(
          iconTheme: context.theme.iconTheme.copyWith(
            color: color,
          ),
        ),
        child: child,
      ),
    );
  }

  /// Will change IconData or ImageProvider to Widget
  static Widget _leadingToWidget(Object? leading, {double size = 25}) {
    assert(leading == null || leading is IconData || leading is ImageProvider || leading is BackCompIcon || leading is Decoration);
    if (leading is IconData && leading == reservedIconSpace)
      return SizedBox(width: size + 16.0);
    if (leading is IconData && leading != reservedIconSpace)
      return Icon(
        leading,
        size: size,
      );
    if (leading is BackCompIcon)
      return leading;
    if (leading is ImageProvider)
      return SizedBox(
        width: size,
        height: size,
        child: Image(image: leading),
      );
    if (leading is Decoration)
      return Container(
        width: 32.0,
        height: 32.0,
        decoration: leading,
      );
    return Container();
  }
}
