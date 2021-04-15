package ipvc.estg.fixity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class AddNoteActivity : AppCompatActivity() {

    private lateinit var titleNoteView: EditText
    private lateinit var descriptionNoteView: EditText
    private lateinit var dateNoteView: TextView

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = supportActionBar

        if (actionBar != null) {

            // Customize the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

            // showing the back button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true)

            // Set toolbar title/app title
            actionBar.setTitle(R.string.titleActivityAddNote)
        }

        dateNoteView = findViewById(R.id.dateTime)
        titleNoteView = findViewById(R.id.edNote_Title)
        descriptionNoteView = findViewById(R.id.edNote_body)

        val dateFormat: String = "dd-MM-yyyy HH:mm" //Date format is Specified
        val strDateFormat =
            SimpleDateFormat(dateFormat) //Date format string is passed as an argument to the Date format object
        val dateNow = Date()
        dateNoteView.text = strDateFormat.format(dateNow)

        val button = findViewById<FloatingActionButton>(R.id.button_addNote)
        button.setOnClickListener {
            val replyIntent = Intent()

            when {
                titleNoteView.text.isNullOrEmpty() -> {
                    Toast.makeText(applicationContext, R.string.note_title_required, Toast.LENGTH_SHORT)
                        .show()
                    //setResult(RESULT_CANCELED, replyIntent)
                }
                descriptionNoteView.text.isNullOrEmpty() -> {
                    Toast.makeText(applicationContext, R.string.note_required, Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    val title = titleNoteView.text.toString()
                    val description = descriptionNoteView.text.toString()

                    replyIntent.putExtra(EXTRA_TITLE, title)
                    replyIntent.putExtra(EXTRA_DESCRIPTION, description)
                    replyIntent.putExtra(EXTRA_DATE, dateNow.toString())
                    setResult(RESULT_OK, replyIntent)
                    finish()
                }
            }
        }

        val cancelButton = findViewById<FloatingActionButton>(R.id.button_cancelAddNote)
        cancelButton.setOnClickListener {
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

    companion object {
        const val EXTRA_TITLE = "com.estg.fixity.messages.TITLE"
        const val EXTRA_DESCRIPTION = "com.estg.fixity.messages.DESCRIPTION"
        const val EXTRA_DATE = "com.estg.fixity.messages.DATE"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}