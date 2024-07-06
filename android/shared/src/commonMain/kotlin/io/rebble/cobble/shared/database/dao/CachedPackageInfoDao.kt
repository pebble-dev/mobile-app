package io.rebble.cobble.shared.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.rebble.cobble.shared.database.entity.CachedPackageInfo

@Dao
interface CachedPackageInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cachedPackageInfo: CachedPackageInfo)

    @Query("SELECT * FROM CachedPackageInfo WHERE id = :packageId")
    suspend fun getPackageInfo(packageId: String): CachedPackageInfo?

    @Query("SELECT * FROM CachedPackageInfo")
    suspend fun getAll(): List<CachedPackageInfo>
}