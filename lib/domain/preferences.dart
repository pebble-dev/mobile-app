import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';

final sharedPreferencesProvider =
    Provider((ref) => SharedPreferences.getInstance());
