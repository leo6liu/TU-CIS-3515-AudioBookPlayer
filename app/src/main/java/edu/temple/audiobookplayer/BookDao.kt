package edu.temple.audiobookplayer

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAll(): List<Book>

    // get book by id
    @Query("SELECT * FROM books WHERE id = :id")
    fun getById(id: Int): Book

    @Query("UPDATE books SET position = :position WHERE id = :id")
    fun updatePosition(id: Int, position: Int)

    @Insert
    fun insertAll(vararg users: Book)

    @Delete
    fun delete(book: Book)

}