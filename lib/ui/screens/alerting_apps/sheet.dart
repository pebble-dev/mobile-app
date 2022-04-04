import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_form.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sheet.g.dart';

enum AppSource {
  @JsonValue("All apps")
  All,
  @JsonValue("Phone only")
  Phone,
  @JsonValue("Watch only")
  Watch,
}

extension on AppSource {
  String toUiString() {
    switch (this) {
      case AppSource.All:
        return tr.alertingAppsFilter.appSource.all;
      case AppSource.Phone:
        return tr.alertingAppsFilter.appSource.phone;
      case AppSource.Watch:
        return tr.alertingAppsFilter.appSource.watch;
    }
  }
}

@JsonSerializable()
class SheetOnChanged {
  final String? query;
  final AppSource? source;

  const SheetOnChanged(this.query, this.source);

  factory SheetOnChanged.fromJson(json) => _$SheetOnChangedFromJson(json);

  Map<String, dynamic> toJson() => _$SheetOnChangedToJson(this);

  static const initial = SheetOnChanged(null, AppSource.All);
}

class Sheet extends HookWidget {
  final VoidCallback onClose;
  final SheetOnChanged initialFilter;
  final ValueChanged<SheetOnChanged> onChanged;

  Sheet({
    Key? key,
    required this.onClose,
    required this.onChanged,
    this.initialFilter = SheetOnChanged.initial,
  })  : super(key: key);

  @override
  Widget build(BuildContext context) {
    final _formHelper = useFormHelper(initialFilter.toJson());
    final iconSize = Theme.of(context).iconTheme.size!;
    return Form(
      key: _formHelper.key,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(tr.alertingAppsFilter.title),
                IconButton(
                  padding: EdgeInsets.zero,
                  constraints: BoxConstraints(
                    maxHeight: iconSize,
                    maxWidth: iconSize,
                  ),
                  icon: Icon(
                    RebbleIcons.x_close,
                    color: context.scheme!.primary,
                  ),
                  onPressed: onClose,
                ),
              ],
            ),
            SizedBox(height: 16),
            TextFormField(
              initialValue: _formHelper.get('query'),
              decoration: InputDecoration(
                labelText: tr.alertingAppsFilter.appName,
              ),
              onSaved: _formHelper.onSaved('query'),
              onFieldSubmitted: (_) async {
                _formHelper.save();
                onChanged(SheetOnChanged.fromJson(_formHelper.model));
              },
            ),
            SizedBox(height: 16),
            DropdownButtonFormField<String>(
              value: _formHelper.get('source'),
              items: AppSource.values
                  .map((source) => DropdownMenuItem(
                        value: _$AppSourceEnumMap[source],
                        child: Text(
                          source.toUiString(),
                        ),
                      ))
                  .toList(),
              onSaved: _formHelper.onSaved('source'),
              onChanged: (value) {
                _formHelper.save();
                onChanged(SheetOnChanged.fromJson(_formHelper.model));
              },
            )
          ],
        ),
      ),
    );
  }
}
