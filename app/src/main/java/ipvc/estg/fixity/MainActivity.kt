package ipvc.estg.fixity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
//import estg.ipvc.fixity.viewModel.NoteListActivity
import ipvc.estg.fixity.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<ImageView>(R.id.btn_notes)

        //button.setOnClickListener {
            //val intent = Intent(this@MainActivity, NoteListActivity::class.java)
            //startActivity(intent)
        //}
    }
}