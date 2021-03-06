import 'dart:async';

import 'package:cobble/domain/entities/base_obj.dart';
import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/infrastructure/datasources/sqlite/data_transfer_objects/baseobj_dto.dart';
import 'package:cobble/infrastructure/datasources/sqlite/filters.dart';
import 'package:cobble/infrastructure/datasources/sqlite/sqlite_tables.dart';
import 'package:flutter/widgets.dart';
import 'package:path/path.dart' as pathlib;
import 'package:sqflite/sqflite.dart';

class SQLiteDataSource {
  static SQLiteDataSource? _databaseHelper; // Singleton DatabaseHelper
  static Database? _database; // Singleton Database

  SQLiteDataSource._createInstance(); // Named constructor to create instance to DatabaseHelper

  factory SQLiteDataSource() {
    if (_databaseHelper == null) {
      _databaseHelper = SQLiteDataSource._createInstance();
    }
    return _databaseHelper!;
  }

  Future<Database> initializeDatabase() async {
    // Get the directory path for both Android and iOS to store database.
    var databasesPath = await (getDatabasesPath() as FutureOr<String>);
    String path = pathlib.join(databasesPath, 'demo.db');

    // Open/close the database at a given path
    Database db = await openDatabase(path, version: 1, onCreate: _createDb);
    return db;
  }

  Future<Database?> get database async {
    if (_database == null) {
      _database = await initializeDatabase();
    }
    return _database;
  }

  Future<void> _createDb(Database db, int newVersion) async {
    await db.execute(SqlCreateTables.createPebbleDevice());
  }

  Future upsert(List<BaseObj> objects) async {
    for (BaseObj obj in objects) {
      await _upsertObj(obj.sqlMap);
    }
  }

  Future delete({
    required Type objType,
    List<CompositeFilter>? compositeFilters,
    List<RelationalFilter>? relationalFilters,
  }) async {
    return await (await this.database)!.delete(
      typeToTable[objType]!,
      where: _filterToSql(compositeFilters, relationalFilters),
    );
  }

  Future<List> read({
    required Type objType,
    List<CompositeFilter>? compositeFilters,
    List<RelationalFilter>? relationalFilters,
    List<CompositeFilter>? recurCompFilters,
  }) async {
    switch (objType) {
      case PebbleScanDevice:
        return [];
      default:
        print("SQL ERROR: Object Type Not Supported!!");
        return [[]];
    }
    return [];
  }

  //////////////////////////////////
  //////    READ
  //////////////////////////////////

  Future<List> _hierarchicalSelect({
    String? recursiveTable,
    String? selectTable,
    String selectColumn = "id",
    Map<String, Map<String, dynamic>>? leftJoins,
    String? orderBy,
    String? recursiveWhere,
    String? where,
    bool groupBy = false,
  }) async {
    var leftJoinsSql =
        (leftJoins != null) ? _prepareJoins(leftJoins, selectTable) : "";
    String orderBySql =
        (orderBy == null || orderBy.length <= 0) ? "" : "ORDER BY $orderBy";
    String recurWhereSql =
        (recursiveWhere == null || recursiveWhere.length <= 0)
            ? ""
            : "WHERE $recursiveWhere";
    String whereSql =
        (where == null || where.length <= 0) ? "" : "WHERE $where";
    String groupBySql = (groupBy) ? "GROUP BY $selectTable.id" : "";
    String qry = '''
      WITH RECURSIVE
        children(pId) AS
          (SELECT id FROM $recursiveTable $recurWhereSql
          UNION
          SELECT id FROM $recursiveTable, children
          WHERE $recursiveTable.parentId = children.pId )
      SELECT * FROM
        $selectTable
        INNER JOIN children ON ($selectTable.$selectColumn = children.pId)
        $leftJoinsSql
      $whereSql
      $groupBySql
      $orderBySql''';

    var result = await (await this.database)!.rawQuery(qry);
    return result;
  }

  Future<List> _select(
    String tableName, {
    String where = "",
    List<String>? whereArgs,
    Map<String, Map<String, dynamic>>? leftJoins,
    String? orderByColumn,
    String? groupByColumn,
  }) async {
    String selectSql = _prepareSelect(tableName, leftJoins);
    String joinSql = _prepareJoins(leftJoins, tableName);
    String whereSql = _prepareWhere(where, whereArgs);
    String orderBySql = (orderByColumn != null)
        ? "ORDER BY " + orderByColumn
        : "ORDER BY $tableName.id ASC";
    String groupBySql =
        (groupByColumn != null) ? "GROUP BY " + groupByColumn : "";
    String query = '''
      SELECT $selectSql, * FROM
        $tableName
        $joinSql
      $whereSql
      $groupBySql
      $orderBySql''';
    List<Map<String, dynamic>> result =
        await (await this.database)!.rawQuery(query);
    return result;
  }

  String _prepareSelect(String tableName, Map? leftJoins) {
    String selQry = "$tableName.id AS '$tableName.id'";
    if (leftJoins == null || leftJoins.length == 0) return selQry;
    for (String joinTable in leftJoins.keys as Iterable<String>)
      selQry += ", $joinTable.id AS '$joinTable.id'";
    return selQry;
  }

  String _prepareWhere(String where, List<String>? whereArgs) {
    if (whereArgs != null && whereArgs.length != 0) {
      for (String arg in whereArgs)
        where = where.replaceFirst(RegExp(r'[?]'), arg);
    }
    return (where.length > 0) ? "WHERE " + where : "";
  }

  String _prepareJoins(
      Map<String, Map<String, dynamic>>? leftJoins, String? tableName) {
    if (leftJoins == null || leftJoins.length == 0) return "";
    List<String> leftJoinsSql = [];
    for (var joinTable in leftJoins.keys) {
      String? fromTable = leftJoins[joinTable]!["from"]["table"];
      var fromColumn = leftJoins[joinTable]!["from"]["colName"];
      String? toColumn = leftJoins[joinTable]!["to"];
      String join = "";

      if (fromColumn is List<String>) {
        String onClause = "";
        for (String fromColumnSingle in fromColumn)
          onClause += "$fromTable.$fromColumnSingle = $joinTable.$toColumn OR ";
        onClause = onClause.substring(0, onClause.length - 4);
        join = "LEFT JOIN $joinTable ON ($onClause)";
      } else {
        join =
            "LEFT JOIN $joinTable ON ($fromTable.$fromColumn = $joinTable.$toColumn)";
      }
      leftJoinsSql.add(join);
    }
    return leftJoinsSql.join(" ");
  }

  //////////////////////////////////
  //////    Filter SQL
  //////////////////////////////////

  final ops = {
    RelationalOperator.equalTo: "=",
    RelationalOperator.greaterThan: "<",
    RelationalOperator.greaterThanOrEqualTo: ">=",
    RelationalOperator.lessThan: "<",
    RelationalOperator.lessThanOrEqualTo: "<=",
  };

  String _filterToSql(
    List<CompositeFilter>? comFilters,
    List<RelationalFilter>? relFilters,
  ) {
    String sql = _parseComFilters("", comFilters);
    sql = _parseRelFilters(sql, relFilters);
    return sql;
  }

  String _parseComFilters(String sql, List<CompositeFilter>? comFilters) {
    if (comFilters == null) return "";
    for (CompositeFilter filter in comFilters) {
      if (sql.length > 0) sql += ", ";
      sql += "${filter.attributeName} IN (";
      for (String val in filter.attributes) sql += val + ", ";
      if (filter.attributes.length > 0) sql = sql.substring(0, sql.length - 2);
      sql += "), ";
    }
    return (sql.length > 1) ? sql.substring(0, sql.length - 2) : sql;
  }

  String _parseRelFilters(String sql, List<RelationalFilter>? relFilters) {
    if (relFilters == null) return sql;
    for (RelationalFilter filter in relFilters) {
      if (filter.value != null &&
          filter.attributeName != null &&
          filter.attributeOperator != null) {
        sql +=
            "${filter.attributeName} ${ops[filter.attributeOperator]} ${filter.value}, ";
      }
    }
    return (sql.length > 1) ? sql.substring(0, sql.length - 2) : sql;
  }

  //////////////////////////////////
  //////    UPDATE & INSERT
  //////////////////////////////////

  Future _upsertObj(Map<String, dynamic> map) async {
    // Loop thru relations within Type
    for (String tableName in map.keys) {
      // Loop thru each obj within a relation
      for (Map<String, dynamic> singleMap in map[tableName]) {
        if (tableName == "name_here_for_only_insert") {
          try {
            await _singleInsert(tableName, singleMap);
          } on DatabaseException catch (exception) {}
        } else if (tableName == "name_here_for_no_id_column")
          await _singleInsertOrUpdate(tableName, singleMap);
        else if (singleMap != null)
          await _singleUpdateOrInsert(tableName, singleMap);
      }
    }
  }

  Future _singleUpdateOrInsert(
      String tableName, Map<String, dynamic> obj) async {
    int updateResult = 0;
    try {
      updateResult = await _singleUpdate(tableName, obj);
    } on DatabaseException catch (exception) {
      //if (exception.isUniqueConstraintError()) {}
    }
    if (updateResult == 0) {
      try {
        await _singleInsert(tableName, obj);
      } on DatabaseException catch (exception) {}
    }
  }

  Future _singleInsertOrUpdate(
      String tableName, Map<String, dynamic> obj) async {
    try {
      await _singleInsert(tableName, obj);
    } on DatabaseException catch (exception) {
      if (exception.isUniqueConstraintError()) {
        try {
          await _singleUpdate(tableName, obj);
        } on DatabaseException catch (exception) {}
      }
    }
  }

  Future _singleInsert(String tableName, Map<String, dynamic> map) async {
    if (map == null || map.length < 1) return false;
    return await (await this.database)!.insert(tableName, map);
  }

  Future<int> _singleUpdate(String tableName, Map map) async {
    if (map == null || map.length < 1 || map["id"] == null) return -1;
    return await (await this.database)!
        .update(tableName, map as Map<String, Object?>, where: "id = ?", whereArgs: [map["id"]]);
  }
}
