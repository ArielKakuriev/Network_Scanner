package com.schoolcomputers.networkscanner.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.schoolcomputers.networkscanner.data.model.ScanSession;

import java.util.List;

@Dao
public interface ScanSessionDao {
    @Insert
    long insert(ScanSession session);

    @Update
    void update(ScanSession session);

    @Query("SELECT * FROM scan_sessions ORDER BY startTime DESC")
    LiveData<List<ScanSession>> getAllSessions();

    @Query("SELECT * FROM scan_sessions ORDER BY startTime DESC LIMIT :limit")
    LiveData<List<ScanSession>> getLatestSessions(int limit);

    @Query("SELECT * FROM scan_sessions WHERE id = :id")
    ScanSession getSessionById(int id);

    @Query("DELETE FROM scan_sessions WHERE startTime < :timestamp")
    void deleteOlderThan(long timestamp);

    @Query("DELETE FROM scan_sessions")
    void deleteAll();
}
