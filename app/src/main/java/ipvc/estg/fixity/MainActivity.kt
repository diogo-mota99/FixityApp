package ipvc.estg.fixity

//import ipvc.estg.fixity.viewModel.NoteListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ipvc.estg.fixity.api.EndPoints
import ipvc.estg.fixity.api.OutputPost
import ipvc.estg.fixity.api.ServiceBuilder
import ipvc.estg.fixity.viewModel.NoteListActivity
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var txt_username: EditText
    private lateinit var txt_password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txt_username = findViewById(R.id.username)
        txt_password = findViewById(R.id.password)

        val button = findViewById<ImageView>(R.id.btn_notes)
        val buttonLogin = findViewById<Button>(R.id.btn_login)

        button.setOnClickListener {
            val intent = Intent(this@MainActivity, NoteListActivity::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            when {
                txt_username.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.username_required,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                txt_password.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.empty_password,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                else -> {

                    val request = ServiceBuilder.buildService(EndPoints::class.java)
                    val call = request.postUserLogin(
                        txt_username.text.toString(),
                        txt_password.text.toString()
                    )

                    call.enqueue(object : retrofit2.Callback<OutputPost> {
                        override fun onResponse(
                            call: Call<OutputPost>,
                            response: Response<OutputPost>
                        ) {
                            if (response.isSuccessful) {

                                val op: OutputPost = response.body()!!

                                if (!op.status) {
                                    when (op.error) {
                                        "username" -> {
                                            Toast.makeText(
                                                this@MainActivity,
                                                R.string.username_not_exist,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        "password" -> {
                                            Toast.makeText(
                                                this@MainActivity,
                                                R.string.password_not_exist,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Welcome " + txt_username.text.toString(),
                                        Toast.LENGTH_LONG
                                    ).show()

                                    //Open MAP ACTIVITY

                                }
                            }
                        }

                        override fun onFailure(call: Call<OutputPost>, t: Throwable) {
                            Toast.makeText(
                                this@MainActivity,
                                R.string.error_register,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                    })
                }


            }
        }
    }

    fun registerClick(view: View) {
        val intentRegister = Intent(this@MainActivity, RegisterActivity::class.java)
        startActivity(intentRegister)
    }
}