package ipvc.estg.fixity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ipvc.estg.fixity.api.EndPoints
import ipvc.estg.fixity.api.OutputPost
import ipvc.estg.fixity.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var txt_name: EditText
    private lateinit var txt_username: EditText
    private lateinit var txt_email: TextView
    private lateinit var txt_pwd: TextView
    private lateinit var txt_confirm_pwd: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //setSupportActionBar(findViewById(R.id.toolbar))

        txt_name = findViewById(R.id.name_register)
        txt_username = findViewById(R.id.username_register)
        txt_email = findViewById(R.id.user_email)
        txt_pwd = findViewById(R.id.password)
        txt_confirm_pwd = findViewById(R.id.password_confirmation)

        val btnRegistar = findViewById<Button>(R.id.btn_register_user)
        val pattern = "\\s".toRegex()

        btnRegistar.setOnClickListener {
            when {
                txt_name.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.name_required,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                txt_username.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.username_required,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                txt_username.text.contains(pattern) -> {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.not_allow_spaces_username),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                txt_email.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.email_required,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(txt_email.text.toString())
                    .matches() -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.email_invalid,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                txt_pwd.text.length < 8 || txt_pwd.text.length > 30 -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.pwd_invalid,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                txt_pwd.text.contains(pattern) -> {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.not_allow_spaces_password),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                txt_confirm_pwd.text.toString() != txt_pwd.text.toString() -> {
                    Toast.makeText(
                        applicationContext,
                        R.string.different_pwd,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                else -> {

                    val request = ServiceBuilder.buildService(EndPoints::class.java)
                    val call = request.postUser(
                        txt_name.text.toString(),
                        txt_username.text.toString(),
                        txt_email.text.toString(),
                        txt_pwd.text.toString()
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
                                                this@RegisterActivity,
                                                R.string.username_exists,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        "email" -> {
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                R.string.email_exists,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        R.string.success_register,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    finish()
                                }
                            }
                        }

                        override fun onFailure(call: Call<OutputPost>, t: Throwable) {
                            Toast.makeText(
                                this@RegisterActivity,
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun loginClick(view: View) {
        finish()
    }
}