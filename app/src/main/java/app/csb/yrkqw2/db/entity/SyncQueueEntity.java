package app.csb.yrkqw2.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_queue")
public class SyncQueueEntity {
    @PrimaryKey(autoGenerate = true)
    public int queueId;
    public String entityType; // Board, List, Card
    public int entityId;
    public String action; // CREATE, UPDATE, DELETE
    public String payload; // JSON payload for the API request
    public String endpoint; // API endpoint to sync with
    public long timestamp;

    public SyncQueueEntity(String entityType, int entityId, String action, String payload, String endpoint, long timestamp) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.payload = payload;
        this.endpoint = endpoint;
        this.timestamp = timestamp;
    }
}
