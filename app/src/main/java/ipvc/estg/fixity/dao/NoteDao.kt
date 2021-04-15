package ipvc.estg.fixity.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ipvc.estg.fixity.entities.Note

@Dao
interface NoteDao {

    @Query("SELECT * FROM note_table ORDER BY date DESC")
    fun getAllNotes(): LiveData<List<Note>>

    /* @Query("SELECT * FROM note_table WHERE id = :id")
     fun getNoteById(id:Int): Note*/

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Query("DELETE FROM note_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

}