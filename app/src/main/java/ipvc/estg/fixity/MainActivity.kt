package ipvc.estg.fixity

//import ipvc.estg.fixity.viewModel.NoteListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import ipvc.estg.fixity.viewModel.NoteListActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<ImageView>(R.id.btn_notes)

        button.setOnClickListener {
            val intent = Intent(this@MainActivity, NoteListActivity::class.java)
            startActivity(intent)
        }


    }

    fun registerClick(view: View) {
        val intentRegister = Intent(this@MainActivity, RegisterActivity::class.java)
        startActivity(intentRegister)
    }
}