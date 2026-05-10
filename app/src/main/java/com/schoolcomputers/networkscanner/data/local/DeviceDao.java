package com.schoolcomputers.networkscanner.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.schoolcomputers.networkscanner.data.model.Device;

import java.util.List;

@Dao
public interface DeviceDao {
    @Insert
    void insert(Device device);

    @Query("SELECT * FROM devices WHERE sessionId = :sessionId ORDER BY ipAddress ASC")
    LiveData<List<Device>> getDevicesForSession(int sessionId);

    @Query("SELECT * FROM devices ORDER BY timestamp DESC")
    LiveData<List<Device>> getAllDevices();
}
