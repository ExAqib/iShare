package com.fyp.iShare.ui.downloads.DB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.lang.reflect.Field;
import java.util.List;

@Dao
public interface FileDao {
    @Query("SELECT * FROM file")
    List<File> getAll();

    @Query("SELECT * FROM file WHERE ID IN (:fileIds)")
    List<File> loadAllByIds(int[] fileIds);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSubject(File file);

    @Query("SELECT * FROM file WHERE ID = :id")
    File getWebhookById(int id);

    @Insert
    void insertAll(File... files);

    @Insert
    long insert(File file);

    @Delete
    void delete(File files);
}
