package com.schoolcomputers.networkscanner.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.schoolcomputers.networkscanner.models.Device;
import com.schoolcomputers.networkscanner.models.ScanRecord;
import com.schoolcomputers.networkscanner.models.ScanWithDevices;

import java.util.List;

/**
 * Room DAO for all scan-related persistence operations.
 * All scan queries are scoped to a specific userId so that
 * users never see each other's scan history.
 */

/** // This interface is a DAO with advanced queries // **/
/** // Room (SQLite): All queries // **/
@Dao
public interface ScanDao {

    // ---- ScanRecord CRUD ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertScan(ScanRecord scan);

    @Update
    void updateScan(ScanRecord scan);

    @Delete
    void deleteScan(ScanRecord scan);

    @Query("DELETE FROM scan_records WHERE userId = :userId")
    void deleteAllScansForUser(String userId);

    @Query("SELECT * FROM scan_records WHERE userId = :userId ORDER BY scannedAt DESC")
    LiveData<List<ScanRecord>> getAllScans(String userId);

    @Query("SELECT * FROM scan_records WHERE id = :id")
    ScanRecord getScanById(long id);

    // ---- Device CRUD ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertDevice(Device device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertDevices(List<Device> devices);

    @Query("SELECT * FROM devices WHERE scanId = :scanId ORDER BY ipAddress ASC")
    LiveData<List<Device>> getDevicesForScan(long scanId);

    @Query("SELECT * FROM devices WHERE scanId = :scanId ORDER BY ipAddress ASC")
    List<Device> getDevicesForScanSync(long scanId);

    // ---- Complex Relation query (JOIN / @Relation) ----

    @Transaction
    @Query("SELECT * FROM scan_records WHERE userId = :userId ORDER BY scannedAt DESC")
    LiveData<List<ScanWithDevices>> getAllScansWithDevices(String userId);

    @Transaction
    @Query("SELECT * FROM scan_records WHERE id = :scanId")
    LiveData<ScanWithDevices> getScanWithDevices(long scanId);

    /**
     * GROUP BY query: devices found per SSID for a specific user.
     */
    @Query("SELECT sr.ssid, COUNT(d.id) AS deviceCount " +
           "FROM scan_records sr " +
           "LEFT JOIN devices d ON d.scanId = sr.id " +
           "WHERE sr.userId = :userId " +
           "GROUP BY sr.ssid " +
           "ORDER BY deviceCount DESC")
    LiveData<List<SsidDeviceCount>> getDeviceCountPerSsid(String userId);

    // ---- Stats helper ----

    @Query("SELECT COUNT(*) FROM scan_records WHERE userId = :userId")
    int getTotalScanCount(String userId);

    @Query("SELECT COUNT(*) FROM devices WHERE scanId = :scanId")
    int getDeviceCountForScan(long scanId);
}
