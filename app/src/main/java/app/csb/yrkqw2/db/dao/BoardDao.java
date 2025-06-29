package app.csb.yrkqw2.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import app.csb.yrkqw2.db.entity.BoardEntity;

@Dao
public interface BoardDao {
    @Insert
    long insertBoard(BoardEntity board);

    @Query("SELECT * FROM boards")
    List<BoardEntity> getAllBoards();

    @Query("DELETE FROM boards")
    void deleteAllBoards();
}
