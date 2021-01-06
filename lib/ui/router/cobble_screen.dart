import 'package:flutter/widgets.dart';

/// Implement (don't need to inherit) this class in order to mark your Widget as
/// suitable target of CobbleNavigator. Classes in Dart are implicitly also
/// interfaces, implementing this class/interface ensures type safety (not
/// everything can be pushed onto Navigator stack) and helps listing all
/// screen in app (by simply looking up all uses of this interface)
///
/// See also:
///
///  * [CobbleNavigator] mixin, for how to push [CobbleScreen] onto stack
abstract class CobbleScreen extends Widget {}
