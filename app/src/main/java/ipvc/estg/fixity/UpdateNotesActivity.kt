package ipvc.estg.fixity

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipvc.estg.fixity.entities.Note
import ipvc.estg.fixity.viewModel.NoteListActivity
import ipvc.estg.fixity.viewModel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

class UpdateNotesActivity : AppCompatActivity() {

    private lateinit var titleNoteUpdate: EditText
    private lateinit var descriptionNoteUpdate: EditText
    private lateinit var dateNoteUpdate: TextView
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_note)

        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = supportActionBar

        if (actionBar != null) {

            // Customize the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

            // showing the back button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true)

            // Set toolbar title/app title
            actionBar.setTitle(R.string.edit_notes_title)
        }

        //view model
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        dateNoteUpdate = findViewById(R.id.update_dateTime)
        titleNoteUpdate = findViewById(R.id.updateNote_Title)
        descriptionNoteUpdate = findViewById(R.id.updateNote_body)
        val updateBtn = findViewById<FloatingActionButton>(R.id.button_updateNote)
        val cancelBtn = findViewById<FloatingActionButton>(R.id.button_cancelUpdateNote)

        val intent: Bundle? = intent.extras
        val title = intent?.getString(NoteListActivity.EXTRA_ITEM_TITLE)
        val description = intent?.getString(NoteListActivity.EXTRA_ITEM_DESCRIPTION)
        val id = intent?.getInt(NoteListActivity.EXTRA_ITEM_ID)

        titleNoteUpdate.setText(title)
        descriptionNoteUpdate.setText(description)

        val dateFormat: String = "dd-MM-yyyy HH:mm" //Date format is Specified
        val strDateFormat =
            SimpleDateFormat(dateFormat) //Date format string is passed as an argument to the Date format object
        val dateNow = Date()
        dateNoteUpdate.text = strDateFormat.format(dateNow)

        updateBtn.setOnClickListener {
            when {
                titleNoteUpdate.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.note_title_required,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    //setResult(RESULT_CANCELED, replyIntent)
                }
                descriptionNoteUpdate.text.isNullOrEmpty() -> {
                    Toast.makeText(applicationContext, R.string.note_required, Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    val titleUpdated = titleNoteUpdate.text.toString()
                    val descriptionUpdated = descriptionNoteUpdate.text.toString()

                    val updatedNote = Note(id, titleUpdated, descriptionUpdated, dateNow)

                    noteViewModel.updateNote(updatedNote)
                    Toast.makeText(this, R.string.success_update_note, Toast.LENGTH_LONG).show()
                    finish()
                }
            }

        }

        cancelBtn.setOnClickListener {
            finish()
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}