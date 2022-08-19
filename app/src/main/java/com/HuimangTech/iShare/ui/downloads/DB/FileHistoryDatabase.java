package com.HuimangTech.iShare.ui.downloads.DB;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = File.class, version = 1)
public abstract class FileHistoryDatabase extends RoomDatabase {
    public abstract FileDao FileDao();
}
