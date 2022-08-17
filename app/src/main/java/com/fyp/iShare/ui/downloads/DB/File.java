package com.fyp.iShare.ui.downloads.DB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class File {


    @PrimaryKey(autoGenerate = true)
    public int ID;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "size")
    private long size;
    @ColumnInfo(name = "type")
    private String type;


    public File(String name, long size) {
        this.name = name;
        this.size = size;
        this.type = "Document";
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
