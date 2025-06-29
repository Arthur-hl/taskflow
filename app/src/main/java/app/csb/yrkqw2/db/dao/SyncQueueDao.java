package app.csb.yrkqw2.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import app.csb.yrkqw2.db.entity.SyncQueueEntity;

@Dao
public interface SyncQueueDao {
    @Insert
    long insert(SyncQueueEntity syncQueueEntity);

    @Delete
    void delete(SyncQueueEntity syncQueueEntity);

    @Query("SELECT * FROM sync_queue")
    List<SyncQueueEntity> getAll();
}
