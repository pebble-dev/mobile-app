import 'package:cobble/domain/entities/base_obj.dart';
import 'package:cobble/infrastructure/datasources/sqlite/sqlite_data_source.dart';

class CommonRepository {
  final SQLiteDataSource localDataSource = SQLiteDataSource();

  Future<void> cache(List objs) async {
    await localDataSource.upsert(objs as List<BaseObj>);
  }
}
