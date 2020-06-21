import 'package:fossil/infrastructure/datasources/sqlite/SqliteDataSource.dart';

class CommonRepository {
  final SQLiteDataSource localDataSource = SQLiteDataSource();

  Future<void> cache(List objs) async {
    await localDataSource.upsert(objs);
  }
}
