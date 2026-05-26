package com.schoolcomputers.networkscanner.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * Room @Relation model joining ScanRecord with its Device list.
 * Used in DAO queries with JOIN semantics for history display.
 */

/** // This class have a DB complex relationships // **/
/** // Room (SQLite): @Relation model // **/
public class ScanWithDevices {

    @Embedded
    public ScanRecord scan;

    @Relation(
        parentColumn = "id",
        entityColumn = "scanId"
    )
    public List<Device> devices;
}
