package ipvc.estg.fixity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import ipvc.estg.fixity.api.EndPoints
import ipvc.estg.fixity.api.Report
import ipvc.estg.fixity.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Response

class ReportDetails : AppCompatActivity() {

    private lateinit var imageProblem: ImageView
    private lateinit var txtProblem: TextView
    private lateinit var txtCategory: TextView
    private lateinit var txtCoordinates: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnRemove: Button
    private lateinit var idProblem: String
    private lateinit var problemDesc: String
    private lateinit var problemCategory: String
    private lateinit var problemLatLng: String
    private var userLogado: Int = 0
    private var userReport: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)

        setSupportActionBar(findViewById(R.id.toolbar))

        //GET DATA FROM MAPS ACTIVITY
        val intentProblem: Bundle? = intent.extras
        idProblem = intentProblem?.getString(MapsActivity.EXTRA_PROBLEMID).toString()
        problemDesc =
            intentProblem?.getString(MapsActivity.EXTRA_PROBLEMDESC).toString().substringAfter("- ")
        problemCategory = intentProblem?.getString(MapsActivity.EXTRA_PROBELMCATEGORY).toString()
        problemLatLng =
            intentProblem?.getString(MapsActivity.EXTRA_LATLNG).toString().substringAfter("(")
                .substringBefore(")")
        userLogado = intentProblem?.getInt(MapsActivity.EXTRA_IDUSERLOGIN)!!

        // calling the action bar
        val actionBar = supportActionBar

        if (actionBar != null) {

            // Customize the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

            // showing the back button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true)

            // Set toolbar title/app title
            actionBar.setTitle(R.string.problem_details)
        }


        imageProblem = findViewById(R.id.imageProblem)
        txtProblem = findViewById(R.id.txtProblem)
        txtCategory = findViewById(R.id.txtCategory)
        txtCoordinates = findViewById(R.id.txtLatLng)
        btnEdit = findViewById(R.id.btn_editProblem)
        btnRemove = findViewById(R.id.btn_deleteProblem)


        Picasso.get().load("https://fixity.pt/myslim/fixity/images/$idProblem.jpeg")
            .into(imageProblem);

        txtProblem.text = problemDesc
        txtCategory.text = problemCategory
        txtCoordinates.text = problemLatLng

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getProblemById(idProblem)

        call.enqueue(object : retrofit2.Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    val report: Report = response.body()!!
                    userReport = report.user_id

                    if (userLogado != userReport) {
                        btnRemove.visibility = View.INVISIBLE
                        btnEdit.visibility = View.INVISIBLE
                    } else {
                        btnRemove.setOnClickListener {
                            /*val requestDelete = ServiceBuilder.buildService(EndPoints::class.java)

                            val builder = AlertDialog.Builder(this@ReportDetails)
                            builder.setPositiveButton(R.string.ok) { _, _ ->
                                val callDelete = requestDelete.deleteProblemById(idProblem)

                                callDelete.enqueue(object : retrofit2.Callback<OutputPost> {
                                    override fun onResponse(
                                        call: Call<OutputPost>,
                                        response: Response<OutputPost>,
                                    ) {
                                        if (response.isSuccessful) {
                                            val op = response.body()!!

                                            if (!op.status) {
                                                when (op.error) {
                                                    "delete" -> {
                                                        Toast.makeText(
                                                            this@ReportDetails,
                                                            R.string.problemDeleteError,
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                    }
                                                    "404" -> {
                                                        Toast.makeText(
                                                            this@ReportDetails,
                                                            R.string.problemNotFound,
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@ReportDetails,
                                                    R.string.problemDeleted,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish()
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<OutputPost>, t: Throwable) {
                                        Toast.makeText(
                                            this@ReportDetails,
                                            R.string.error_register,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })


                            }
                            builder.setNegativeButton(R.string.cancel) { _, _ ->
                            }

                            builder.setMessage(R.string.delete_problem_confirmation)
                            builder.create().show()
                        */
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Toast.makeText(this@ReportDetails, R.string.error_register, Toast.LENGTH_SHORT)
                    .show()
            }

        })

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
