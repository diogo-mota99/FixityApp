package ipvc.estg.fixity.viewModel

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipvc.estg.fixity.AddNoteActivity
import ipvc.estg.fixity.R
import ipvc.estg.fixity.ShowNoteDetails
import ipvc.estg.fixity.adapters.NoteAdapter
import ipvc.estg.fixity.entities.Note
import java.util.*


class NoteListActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var noteViewModel: NoteViewModel
    private val newNoteActivityRequestCode = 1
    private lateinit var noteModel: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_list)


        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = supportActionBar

        if (actionBar != null) {

            // Customize the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

            // showing the back button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true)

            // Set toolbar title/app title
            actionBar.setTitle(R.string.note_title)
        }


        //recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.notes_recycler)
        val adapter = NoteAdapter(
            this, onClickListener = this/*, onDeletePressed =
            DeleteBtnClick(), onEditPressed = EditBtnClick()*/
        )
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)

        //view model
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        noteViewModel.allNotes.observe(this, Observer { notes ->
            notes?.let {
                adapter.setNotes(notes)
            }
        })

        //Fab
        val fab = findViewById<FloatingActionButton>(R.id.btn_add)
        fab.setOnClickListener {
            val intent = Intent(this@NoteListActivity, AddNoteActivity::class.java)
            startActivityForResult(intent, newNoteActivityRequestCode)
        }
    }

    // this event will enable the back
    // function to the button on press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, result: Int, data: Intent?) {
        super.onActivityResult(requestCode, result, data)

        if (requestCode == newNoteActivityRequestCode && result == RESULT_OK) {
            val title = data?.getStringExtra(AddNoteActivity.EXTRA_TITLE)
            val description = data?.getStringExtra(AddNoteActivity.EXTRA_DESCRIPTION)
            val date = Date(data?.getStringExtra(AddNoteActivity.EXTRA_DATE))
            val note = Note(title = title, description = description, date = date)

            noteViewModel.insert(note)
        }
    }

    override fun onClick(v: View?) {
        noteModel = v?.tag as Note
        val intent = Intent(this@NoteListActivity, ShowNoteDetails::class.java)
        val bundle = Bundle()
        bundle.putSerializable(EXTRA_SELECTED_NOTE, noteModel)
        intent.putExtras(bundle)
        startActivity(intent)
    }


    /*inner class DeleteBtnClick : View.OnClickListener {
        override fun onClick(v: View?) {
            noteModel = v?.tag as Note
            val builder = AlertDialog.Builder(this@NoteListActivity)
            builder.setPositiveButton(R.string.ok) { _, _ ->
                noteViewModel.deleteNote(noteModel)
                Toast.makeText(applicationContext, R.string.success_delete_note, Toast.LENGTH_LONG)
                    .show()
            }
            builder.setNegativeButton(R.string.cancel) { _, _ ->
            }

            builder.setMessage(R.string.delete_confirmation)
            builder.create().show()
        }
    }*/

    /*inner class EditBtnClick : View.OnClickListener {
        override fun onClick(v: View?) {
            noteModel = v?.tag as Note
            val intent = Intent(this@NoteListActivity, UpdateNotesActivity::class.java)
            intent.putExtra(EXTRA_ITEM_TITLE, noteModel.title)
            intent.putExtra(EXTRA_ITEM_DESCRIPTION, noteModel.description)
            intent.putExtra(EXTRA_ITEM_ID, noteModel.id)
            startActivity(intent)
        }
    }*/

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    companion object {
        const val EXTRA_ITEM_TITLE = "com.estg.fixity.messages.ITEM"
        const val EXTRA_ITEM_DESCRIPTION = "com.estg.fixity.messages.DESCRIPTION"
        const val EXTRA_ITEM_ID = "com.estg.fixity.messages.ID"
        const val EXTRA_SELECTED_NOTE = "com.estg.fixity.messages.SELECTED_NOTE"
    }
}