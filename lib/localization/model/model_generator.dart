import 'dart:convert';

import 'package:build/build.dart';
import 'package:glob/glob.dart';
import 'package:recase/recase.dart';
import 'package:source_gen/source_gen.dart';

Builder modelGenerator(BuilderOptions options) => LibraryBuilder(
      ModelGenerator(),
      generatedExtension: '.model.dart',
    );

class ModelGenerator extends Generator {
  @override
  Future<String> generate(LibraryReader library, BuildStep buildStep) async {
    try {
      final langs = await _findLangs(buildStep);
      _validateLangs(langs);
      final models = _extractModels(langs.first);
      return _generateFile(models);
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

  void _compareJson(JsonFragment a, JsonFragment b) {
    a.fragment.forEach((key, value) {
      final path = '${a.path}.$key';
      if (value == null) {
        throw AssertionError(
          "${a.file} contains null value at $path!",
        );
      } else if (value is num) {
        throw AssertionError(
          "${a.file} contains num value at $path, only strings are supported!",
        );
      } else if (value is bool) {
        throw AssertionError(
          "${a.file} contains bool value at $path, only strings are supported!",
        );
      } else if (value is List) {
        throw AssertionError(
          "${a.file} contains list at $path, only strings are supported!",
        );
      } else if (value is String) {
        if (!b.fragment.containsKey(key))
          throw AssertionError(
            "${a.file} contains $path but ${b.file} doesn't!",
          );
      } else if (value is Map) {
        _compareJson(
          JsonFragment.append(a, key),
          JsonFragment.append(b, key),
        );
      } else {
        throw AssertionError(
          "${a.file} contains unknown type ${value.runtimeType} at $path, "
          "I hope you know what you are doing.",
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

  List<Model> _extractModelsInFragment(JsonFragment fragment) {
    final fields = fragment.fragment.keys.map((key) {
      if (fragment.fragment[key] is String) {
        return Field('String', key.camelCase);
      } else {
        return Field('${fragment.path}.$key'.pascalCase, key.camelCase);
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
        .expand((e) => e)
        .toList();
    return [model, ...children];
  }

  String _generateModel(Model model) {
    final className = model.name.pascalCase;
    final text = '''
@Model()
class $className {
${model.fields.map((field) => '''
  @Field()
  final ${field.type} ${field.name};
''').join('\n')}

  $className(${model.fields.map((f) => 'this.${f.name}').join(', ')});

  factory $className.fromJson(Map<String, dynamic> json) => _\$${className}FromJson(json);
}
''';
    return text;
  }

  String _generateFile(List<Model> models) {
    final text = '''
import 'package:cobble/localization/model/annotations.dart';
import 'package:json_annotation/json_annotation.dart';

part 'model_generator.model.g.dart';

${models.map((model) => _generateModel(model)).join('\n\n')}
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

class Model {
  final String name;
  final List<Field> fields;

  /// Pass in full path (root.key1.key2.key3) because class name is converted
  /// from dot.case to PascalCase, meaning each key will get unique class name
  /// as long as it doesn't have any sibling with same key
  Model(String path, this.fields) : this.name = path.pascalCase;
}

class Field {
  final String type;
  final String name;

  Field(this.type, this.name);
}
