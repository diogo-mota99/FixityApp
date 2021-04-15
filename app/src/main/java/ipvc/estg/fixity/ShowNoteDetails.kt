package ipvc.estg.fixity

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ipvc.estg.fixity.viewModel.NoteListActivity
import ipvc.estg.fixity.entities.Note

class ShowNoteDetails : AppCompatActivity() {

    private var noteModel: Note? = null
    var textNoteTitle: TextView? = null
    var textNoteDesc: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_note_details)

        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = supportActionBar

        if (actionBar != null) {

            // Customize the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

            // showing the back button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true)

            // Set toolbar title/app title
            actionBar.setTitle(R.string.note_details)
        }

        noteModel = intent.extras!!.getSerializable(NoteListActivity.EXTRA_SELECTED_NOTE) as Note

        val noteTitle = noteModel?.title
        val noteDesc = noteModel?.description

        textNoteTitle = findViewById(R.id.noteTitle)
        textNoteDesc = findViewById(R.id.noteContent)

        if (noteModel != null && !TextUtils.isEmpty(noteTitle)) {

            textNoteTitle?.text = noteTitle

            if (!TextUtils.isEmpty(noteTitle)) {
                textNoteDesc?.text = noteDesc
            }
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