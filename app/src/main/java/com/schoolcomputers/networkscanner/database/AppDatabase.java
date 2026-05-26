package com.schoolcomputers.networkscanner.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.schoolcomputers.networkscanner.models.Device;
import com.schoolcomputers.networkscanner.models.ScanRecord;

/**
 * Room (SQLite) database for scan data only.

 * User accounts are handled by Firebase Auth + Firestore — NOT stored here.
 * This database only contains ScanRecord and Device tables.
 */

/** // Firebase: Reference firebase // **/
/** // Room (SQLite): DB definition // **/
@Database(
    entities = {ScanRecord.class, Device.class},
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "network_scanner.db";

    public abstract ScanDao scanDao();

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
