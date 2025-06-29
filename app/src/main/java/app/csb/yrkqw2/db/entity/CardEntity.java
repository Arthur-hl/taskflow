package app.csb.yrkqw2.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cards")
public class CardEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int listId;
    public String title;
    public String description;
    public String createdAt;
}
