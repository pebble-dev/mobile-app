import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/theme/cobble_scheme.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';

class CobbleCardAction {
  final VoidCallback onPressed;
  final String? label;
  final IconData? icon;

  const CobbleCardAction({
    Key? key,
    required this.onPressed,
    this.label,
    this.icon,
  }) : assert(
          label is String && label.length > 0 || icon is IconData,
          "CardAction needs label and/or icon",
        );
}

/// A Cobble design card, slightly elevated material used to visually group
/// information.
///
/// [leading] must be provided and can be [IconData] or [ImageProvider].
/// [intent] will change background of card and appearance of card button.
///
/// See also:
///
///  * [Svg], to use .svg as [ImageProvider], from package `flutter_svg_provider`
class CobbleCard extends StatelessWidget {
  final Object leading;
  final String title;
  final String? subtitle;
  final Widget? child;
  final List<CobbleCardAction> actions;
  final Color? intent;
  final EdgeInsets padding;
  final GestureTapCallback? onClick;

  const CobbleCard({
    Key? key,
    required this.leading,
    required this.title,
    this.subtitle,
    this.child,
    this.actions = const [],
    this.intent,
    this.padding = const EdgeInsets.all(0),
    this.onClick,
  })  : assert(
          leading is IconData || leading is ImageProvider,
          'Leading can be only IconData and ImageProvider',
        ),
        assert(title != null && title.length > 0),
        assert(subtitle == null || subtitle.length > 0),
        assert(actions != null),
        assert(padding != null),
        super(key: key);

  factory CobbleCard.inList({
    required Object leading,
    required String title,
    String? subtitle,
    Widget? child,
    List<CobbleCardAction> actions = const [],
    Color? intent,
    GestureTapCallback? onClick,
  }) =>
      CobbleCard(
        leading: leading,
        title: title,
        subtitle: subtitle,
        child: child,
        actions: actions,
        intent: intent,
        padding: const EdgeInsets.all(16),
        onClick: onClick,
      );

  @override
  Widget build(BuildContext context) {
    final isColored = intent != null;
    final brightness = isColored
        ? ThemeData.estimateBrightnessForColor(intent!)
        : context.scheme!.brightness;
    final scheme = CobbleSchemeData.fromBrightness(brightness);


    final content = Column(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                ),
                clipBehavior: Clip.antiAlias,
                child: leading is IconData
                    ? Container(
                  decoration: BoxDecoration(
                    color: scheme.invert().surface,
                  ),
                  child: Icon(
                    leading as IconData?,
                    color: scheme.invert().text,
                  ),
                )
                    : Image(image: leading as ImageProvider<Object>),
              ),
              SizedBox(width: 16),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: context.textTheme.headline6!.copyWith(
                      color: scheme.text,
                    ),
                  ),
                  if (subtitle != null) ...[
                    SizedBox(height: 4),
                    Text(
                      subtitle!,
                      style: context.textTheme.bodyText2!.copyWith(
                        color: scheme.text,
                      ),
                    ),
                  ],
                ],
              ),
            ],
          ),
        ),
        if (child != null)
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
            child: child,
          ),
        if (actions.isNotEmpty)
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 8, 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: actions
                  .expand(
                    (action) => [
                  SizedBox(width: 8),
                  CobbleButton(
                    onPressed: action.onPressed,
                    label: action.label,
                    icon: action.icon,
                    outlined: isColored,
                  ),
                ],
              )
                  .toList()
                  .sublist(1),
            ),
          ),
      ],
    );

    Widget card = Card(
      color: intent,
      margin: padding,
      child: onClick != null ?
          InkWell(
            child: content,
            onTap: onClick,
          ) :
          content
    );
    if (isColored)
      card = CobbleButton.withColor(
        color: scheme.text,
        child: card,
      );
    return card;
  }
}
