package app.csb.yrkqw2.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lists")
public class ListEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int boardId;
    public String title;
    public String createdAt;
}
