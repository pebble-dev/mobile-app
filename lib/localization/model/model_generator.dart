import 'dart:convert';

import 'package:build/build.dart';
import 'package:glob/glob.dart';
import 'package:recase/recase.dart';
import 'package:source_gen/source_gen.dart';

Builder modelGenerator(BuilderOptions options) => LibraryBuilder(
      ModelGenerator(),
      generatedExtension: '.model.dart',
    );

/// Generator that validates `.json` files and generate statically typed
/// model based on their structure. This model is annotated with annotations from
/// `json_serializable` that allows it to automatically generate JSON parser.
///
/// Rules for `.json` files:
/// * keys can be nested
/// * keys must be snake_case
/// * values must be non-empty strings or JSON objects
/// * values can include parameters, '{} or {named}'
/// * named parameters must be camelCase
class ModelGenerator extends Generator {
  @override
  Future<String?> generate(LibraryReader library, BuildStep buildStep) async {
    try {
      final langs = await _findLangs(buildStep);
      _validateLangs(langs);
      final models = _extractModels(langs.first);
      final locales = _extractLocales(langs);
      return _generateFile(models, locales);
    } catch (e) {
      print(e);
      return null;
    }
  }

  Future<List<Lang>> _findLangs(BuildStep buildStep) =>
      buildStep.findAssets(Glob('lang/*.json')).asyncMap(
        (assetId) async {
          final content = await buildStep.readAsString(assetId);
          return Lang(assetId, json.decode(content));
        },
      ).toList();

  void _validateLangs(List<Lang> langs) {
    langs.forEach((lang) {
      _validateFragment(JsonFragment.fromLang(lang));
    });
    if (langs.length <= 1) return;
    langs.skip(1).forEach((lang) {
      _compareJson(
        JsonFragment.fromLang(lang),
        JsonFragment.fromLang(langs.first),
      );
      _compareJson(
        JsonFragment.fromLang(langs.first),
        JsonFragment.fromLang(lang),
      );
    });
  }

  /// Validate fragment/value. If value is another Map, it will recursively call
  /// this function with nested Map.
  void _validateFragment(JsonFragment fr) {
    fr.fragment.forEach((key, value) {
      final path = '${fr.path}.$key';
      if (key.snakeCase != key) {
        throw AssertionError(
          "${fr.file} contains $path which isn't using snake_case!",
        );
      } else if (value == null) {
        throw AssertionError(
          "${fr.file} contains null value at $path!",
        );
      } else if (value is num) {
        throw AssertionError(
          "${fr.file} contains num value at $path, only strings are supported!",
        );
      } else if (value is bool) {
        throw AssertionError(
          "${fr.file} contains bool value at $path, only strings are supported!",
        );
      } else if (value is List) {
        throw AssertionError(
          "${fr.file} contains list at $path, only strings are supported!",
        );
      } else if (value is String) {
        if (value.isEmpty) {
          throw AssertionError(
            "${fr.file} contains empty string at $path!",
          );
        }
      } else if (value is Map) {
        _validateFragment(JsonFragment.append(fr, key));
      } else {
        throw AssertionError(
          "${fr.file} contains unknown type ${value.runtimeType} at $path, "
          "I hope you know what you are doing.",
        );
      }
    });
  }

  /// Compare structure of 2 Maps to see if both contain same keys.
  void _compareJson(JsonFragment a, JsonFragment b) {
    a.fragment.forEach((key, value) {
      final path = '${a.path}.$key';
      if (!b.fragment.containsKey(key)) {
        throw AssertionError(
          "${a.file} contains $path but ${b.file} doesn't!",
        );
      }
      if (value is Map) {
        _compareJson(
          JsonFragment.append(a, key),
          JsonFragment.append(b, key),
        );
      }
    });
  }

  List<Model> _extractModels(Lang lang) {
    final root = JsonFragment.fromLang(lang);
    final models = _extractModelsInFragment(root);
    models.sort((a, b) {
      final difference = a.name.compareTo(b.name);
      if (difference == 0) {
        throw AssertionError(
          "${a.name.dotCase} is duplicate!",
        );
      }
      return difference;
    });
    return models;
  }

  List<String> _extractLocales(List<Lang> langs) {
    return langs.map((lang) {
      final filename = lang.assetId.path.split('/').last;
      final match =
          RegExp(r'(?<languageCode>\S+?)(?:[-_](?<countryCode>\S+?))?\.json')
              .firstMatch(
        filename,
      );

      /// dart:ui (from Flutter engine) isn't available in generators so we
      /// can't use Locale class. We must generate output string directly from
      /// regex match. :(
      final languageCode = match?.namedGroup('languageCode');
      final countryCode = match?.namedGroup('countryCode');
      if (languageCode != null && countryCode != null) {
        return "Locale('$languageCode', '$countryCode')";
      } else {
        if (languageCode != null) {
          return "Locale('$languageCode')";
        }
      }

      throw AssertionError(
        "$filename isn't recognized as locale, "
        "file's name should be the same as output of Locale().toString()",
      );
    }).toList();
  }

  List<Model> _extractModelsInFragment(JsonFragment fragment) {
    final fields = fragment.fragment.keys.map((key) {
      if (fragment.fragment[key] is String) {
        return Field('String', key, value: fragment.fragment[key]);
      } else {
        return Field('${fragment.path}.$key'.pascalCase, key);
      }
    }).toList();
    final model = Model(fragment.path, fields);
    final children = fragment.fragment.keys
        .map((key) {
          final value = fragment.fragment[key];
          if (value is! Map) {
            return null;
          }
          return _extractModelsInFragment(JsonFragment.append(fragment, key));
        })
        .where((element) => element != null)
        .expand((e) => e!)
        .toList();
    return [model, ...children];
  }

  /// Generate helpers for named and positional parameters
  String _generateHelpers() {
    return '''
String _args(String value,
  List<String> positional,
  Map<String, String> named,
) {
  named.forEach(
    (key, _value) => value = value.replaceAll(RegExp('{\$key}'), _value),
  );
  positional.forEach((str) => value = value.replaceFirst(RegExp(r'{}'), str));
  return value;
}
''';
  }

  /// Generate field with @JsonKey
  String _generateField(Field field) {
    if (!field.hasParams) {
      return '''
  @JsonKey(
    name: '${field.sourceName}',
    required: true,
    disallowNullValue: true,
  )
  final ${field.type} ${field.publicName};
''';
    }

    final positionalArguments = List.generate(
      field.positional.length,
      (index) => 'String pos$index, ',
    ).join('');
    final positional = '[${List.generate(
      field.positional.length,
      (index) => 'pos$index, ',
    ).join('')}]';

    String namedArguments = List.generate(
      field.named.length,
      (index) => 'required String ${field.named[index].group(1)}, ',
    ).join('');
    if (namedArguments.isNotEmpty) {
      namedArguments = '{$namedArguments}';
    }
    final named = '{${List.generate(
      field.named.length,
      (index) =>
          "'${field.named[index].group(1)}': ${field.named[index].group(1)}, ",
    ).join('')}}';

    return '''
  @JsonKey(
    name: '${field.sourceName}',
    required: true,
    disallowNullValue: true,
  )
  @Deprecated('This localized string requires parameters, use ${field.publicName}() instead')
  final ${field.type} ${field.publicName}Raw;
  ${field.type} ${field.publicName}($positionalArguments$namedArguments) =>
    _args(
      ${field.publicName}Raw, // ignore: deprecated_member_use_from_same_package
      $positional,
      $named,
    );
''';
  }

  /// Generate constructor for class
  String _generateConstructor(Model model) {
    final className = model.name.pascalCase;
    final args = model.fields.map((f) {
      if (f.hasParams)
        return 'this.${f.publicName}Raw';
      else
        return 'this.${f.publicName}';
    }).join(', ');
    String attempt = '$className($args);';
    if (attempt.length > 77) {
      attempt = '$className($args,);';
    }
    return attempt;
  }

  /// Generate class with @JsonSerializable and unique name
  String _generateModel(Model model) {
    final className = model.name.pascalCase;
    final text = '''
@JsonSerializable(
  createToJson: false,
  disallowUnrecognizedKeys: true,
)
class $className {
${model.fields.map(_generateField).join('\n')}

  ${_generateConstructor(model)}

  factory $className.fromJson(Map<String, dynamic> json) => _\$${className}FromJson(json);
}
''';
    return text;
  }

  /// Generate entire file, with imports, helpers, models and list of supported
  /// locales.
  String _generateFile(List<Model> models, List<String> locales) {
    final text = '''
import 'package:json_annotation/json_annotation.dart';
import 'dart:ui';

part 'model_generator.model.g.dart';

${_generateHelpers()}

${models.map((model) => _generateModel(model)).join('\n\n')}

final supportedLocales = [
${locales.map((l) => '  $l,').join('\n')}
];
''';
    return text;
  }
}

class Lang {
  final AssetId assetId;
  final Map<String, dynamic> source;

  Lang(this.assetId, this.source);
}

class JsonFragment {
  final String file;
  final String path;
  final Map<String, dynamic> fragment;

  JsonFragment(this.file, this.path, this.fragment);

  factory JsonFragment.append(JsonFragment parent, String key) => JsonFragment(
        parent.file,
        '${parent.path}.$key',
        parent.fragment[key],
      );

  factory JsonFragment.fromLang(Lang lang) => JsonFragment(
        lang.assetId.path,
        'language',
        lang.source,
      );
}

/// Data container for JSON object. Each JSON object in `.json` file gets
/// converted to [Model].
class Model {
  final String name;
  final List<Field> fields;

  /// Pass in full path (root.key1.key2.key3) because class name is converted
  /// from dot.case to PascalCase, meaning each key will get unique class name
  /// as long as it doesn't have any sibling with same key
  Model(String path, this.fields) : this.name = path.pascalCase;
}

/// Data container for JSON value. Each JSON value gets converted to [Field],
/// be it [String] or another [Model].
class Field {
  final String type;
  final String sourceName;
  final String publicName;
  final List<RegExpMatch> positional;
  final List<RegExpMatch> named;

  Field._(
    this.type,
    this.sourceName,
    this.publicName,
    this.positional,
    this.named,
  );

  factory Field(String type, String name, {String? value}) {
    final positional = RegExp(r'{}').allMatches(value ?? '').toList();
    final named = RegExp(r'{(\S+?)}').allMatches(value ?? '').toList();
    named.forEach((m) {
      final param = m.group(1);
      if (param.camelCase != param) {
        throw AssertionError(
          "String '$name' contains named parameter '$param' with invalid case, "
          "only camelCased named parameters are supported.",
        );
      }
    });
    if (positional.isNotEmpty || named.isNotEmpty) {
      assert(type == 'String');
      return Field._(
        type,
        name,
        name.camelCase,
        positional,
        named,
      );
    } else {
      return Field._(
        type,
        name,
        name.camelCase,
        [],
        [],
      );
    }
  }

  bool get hasParams => positional.isNotEmpty || named.isNotEmpty;
}
