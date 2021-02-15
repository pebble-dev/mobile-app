import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';

/// Simplify reading values from form after it was validated and saved
///
/// Usage:
/// ```dart
/// @override
/// Widget build(BuildContext context) {
///   final formHelper = useFormHelper({
///     'username': 'default value',
///   });
///   return Form(
///     key: formHelper.key,
///     child: TextFormField(
///       initialValue: formHelper.get('username'),
///       onSaved: formHelper.onSaved('username'),
///       onFieldSubmitted: () async {
///         if(!formHelper.validate()) return;
///         formHelper.save();
///         print(formHelper.model);
///       },
///     ),
///   );
/// }
/// ```
class FormHelper {
  final key = GlobalKey<FormState>();
  final model;

  FormHelper([this.model = const {}]);

  /// Simple HoF you can use as `onSaved` callback on any field
  FormFieldSetter<dynamic> onSaved(String name) =>
      (value) => model[name] = value;

  /// Returns current value of field, usually used to set `initialValue` of field
  dynamic get(String name) => model[name];

  /// Validates all input fields, same as calling `formKey.currentState.validate()`
  bool validate() => key.currentState?.validate() ?? false;

  /// Saves all input fields, same as calling `formKey.currentState.save()`
  void save() => key.currentState?.save();
}

/// Hook that creates and returns [FormHelper].
FormHelper useFormHelper([Map<String, dynamic> defaultValue = const {}]) {
  final result = useState(FormHelper(defaultValue));
  return result.value;
}
