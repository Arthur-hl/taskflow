package app.csb.yrkqw2.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import app.csb.yrkqw2.db.entity.ListEntity;

@Dao
public interface ListDao {
    @Insert
    long insertList(ListEntity list);

    @Query("SELECT * FROM lists WHERE boardId = :boardId")
    List<ListEntity> getListsByBoardId(int boardId);

    @Query("DELETE FROM lists WHERE boardId = :boardId")
    void deleteListsByBoardId(int boardId);
}
