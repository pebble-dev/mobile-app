import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/theme/cobble_scheme.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

/// Displays Cobble-styled dialog.
///
/// You must provide [title] and/or [content]. Provide [negative] and [positive]
/// labels if you wish to display action buttons below dialog's body, both are
/// optional. If [dismissible] flag is true, user can dismiss dialog by tapping
/// on dark background around dialog, this is same as tapping on [negative]
/// action. [intent] can be used for variant styling of dialog.
///
/// Returns a [Future] that resolves to true/false, depending on action user
/// has tapped on.
Future<bool> showCobbleDialog({
  @required BuildContext context,
  String title,
  String content,
  String negative,
  String positive,
  bool dismissible = true,
  Color intent,
}) async {
  assert(context is BuildContext);
  assert(
    title is String || content is String,
    "Provide title and/or content.",
  );
  assert(dismissible is bool);

  final result = await showDialog<bool>(
    context: context,
    useSafeArea: false,
    barrierDismissible: dismissible,
    builder: (_) => CobbleDialog(
      title: title,
      content: content,
      negative: negative,
      positive: positive,
      intent: intent,
    ),
  );
  return result ?? false;
}

class CobbleDialog extends StatelessWidget {
  final String title;
  final String content;
  final String negative;
  final String positive;
  final Color intent;

  const CobbleDialog({
    Key key,
    this.title,
    this.content,
    this.negative,
    this.positive,
    this.intent,
  })  : assert(
          title is String || content is String,
          "Provide title and/or content.",
        ),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    final isColored = intent != null;
    final brightness = isColored
        ? ThemeData.estimateBrightnessForColor(intent)
        : context.scheme.brightness;
    final scheme = CobbleSchemeData.fromBrightness(brightness);

    Widget dialog = Dialog(
      backgroundColor: intent,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (title is String)
              Text(
                title,
                style: context.textTheme.headline6.copyWith(
                  color: scheme.text,
                ),
              ),
            if (title is String && content is String) SizedBox(height: 4),
            if (content is String)
              Text(
                content,
                style: context.textTheme.bodyText2.copyWith(
                  color: scheme.text,
                ),
              ),
            if (negative is String || positive is String) ...[
              SizedBox(height: 8),
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  if (negative is String)
                    CobbleButton(
                      outlined: isColored,
                      onPressed: () {
                        Navigator.of(context).pop(false);
                      },
                      label: negative,
                    ),
                  if (negative is String && positive is String)
                    SizedBox(width: 8),
                  if (positive is String)
                    CobbleButton(
                      outlined: isColored,
                      onPressed: () {
                        Navigator.of(context).pop(true);
                      },
                      label: positive,
                    ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
    if (isColored)
      dialog = CobbleButton.withColor(
        color: scheme.text,
        child: dialog,
      );
    return dialog;
  }
}
