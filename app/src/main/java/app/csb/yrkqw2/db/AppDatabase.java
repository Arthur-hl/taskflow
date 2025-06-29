package app.csb.yrkqw2.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import app.csb.yrkqw2.db.dao.BoardDao;
import app.csb.yrkqw2.db.dao.CardDao;
import app.csb.yrkqw2.db.dao.ListDao;
import app.csb.yrkqw2.db.dao.SyncQueueDao;
import app.csb.yrkqw2.db.entity.BoardEntity;
import app.csb.yrkqw2.db.entity.CardEntity;
import app.csb.yrkqw2.db.entity.ListEntity;
import app.csb.yrkqw2.db.entity.SyncQueueEntity;
import app.csb.yrkqw2.db.util.Converters;

@Database(entities = {BoardEntity.class, ListEntity.class, CardEntity.class, SyncQueueEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract BoardDao boardDao();
    public abstract ListDao listDao();
    public abstract CardDao cardDao();
    public abstract SyncQueueDao syncQueueDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "taskflow_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
