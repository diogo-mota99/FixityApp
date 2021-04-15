package ipvc.estg.fixity.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ipvc.estg.fixity.db.NoteDB
import ipvc.estg.fixity.db.NoteRepository
import ipvc.estg.fixity.entities.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    val allNotes: LiveData<List<Note>>

    init {
        val notesDao = NoteDB.getDatabase(application, viewModelScope).noteDao()
        repository = NoteRepository(notesDao)
        allNotes = repository.allNotes
    }

    fun deleteNote(note: Note) =
        viewModelScope.launch(Dispatchers.IO) { repository.deleteNote(note) }

    fun updateNote(note: Note) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateNote(note) }

    fun insert(note: Note) = viewModelScope.launch(Dispatchers.IO) { repository.insert(note) }

}