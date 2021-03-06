import 'package:flutter/widgets.dart';

/// ```dart
/// String attributeName; // The attribute name
/// List<String> attributes; // List of values attribute can be
/// ```
/// Holds data for filters that select several values that are in a list.
///
/// SQL example:
/// ```sql
/// WHERE id IN (1,2,3,4,5)
/// ```
class CompositeFilter {
  String attributeName;
  List<String> attributes;
  CompositeFilter({
    required this.attributeName,
    required this.attributes,
  });
  String toString() {
    return '''CompositeFilter{
      attributeName:${this.attributeName},
      attributes:${this.attributes}
    }''';
  }
}

/// ```dart
/// String attributeName; // The attribute name
/// String value; // The value to be compared with
/// RelationalOperator attributeOperator; // enum
/// ```
/// Holds data for filters that show a relation to a single value such as <, <=, >, >, and =
///
/// SQL example:
/// ```sql
/// WHERE date >= "2020-04-10"
/// ```
///
class RelationalFilter {
  String attributeName;
  String value;
  RelationalOperator attributeOperator;
  RelationalFilter({
    required this.attributeName,
    required this.value,
    required this.attributeOperator,
  });
}

/// enum values:
/// * lessThan
/// * lessThanOrEqualTo
/// * greaterThan
/// * greaterThanOrEqualTo
/// * equalTo
enum RelationalOperator {
  lessThan,
  lessThanOrEqualTo,
  greaterThan,
  greaterThanOrEqualTo,
  equalTo,
  notEqualTo
}
