package app.csb.yrkqw2.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "boards")
public class BoardEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String createdAt;
}
