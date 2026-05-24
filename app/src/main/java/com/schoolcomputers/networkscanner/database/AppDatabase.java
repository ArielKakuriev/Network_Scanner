package com.networkscanner.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.networkscanner.models.Device;
import com.networkscanner.models.ScanRecord;

/**
 * Room database for the Network Scanner app.
 * Singleton pattern ensures only one instance is created.
 */
@Database(
    entities = {ScanRecord.class, Device.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "network_scanner.db";

    public abstract ScanDao scanDao();

    /**
     * Returns the singleton database instance, creating it if necessary.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
