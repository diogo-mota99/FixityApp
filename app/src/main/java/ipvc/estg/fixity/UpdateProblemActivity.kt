package ipvc.estg.fixity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class UpdateProblemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_problem)

        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = supportActionBar

        if (actionBar != null) {

            // Customize the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

            // showing the back button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true)

            // Set toolbar title/app title
            actionBar.setTitle(R.string.edit_problem_title)
        }
    }
}