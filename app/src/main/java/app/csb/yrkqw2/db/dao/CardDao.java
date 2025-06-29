package app.csb.yrkqw2.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import app.csb.yrkqw2.db.entity.CardEntity;

@Dao
public interface CardDao {
    @Insert
    long insertCard(CardEntity card);

    @Query("SELECT * FROM cards WHERE listId = :listId")
    List<CardEntity> getCardsByListId(int listId);

    @Query("DELETE FROM cards WHERE listId = :listId")
    void deleteCardsByListId(int listId);
}
