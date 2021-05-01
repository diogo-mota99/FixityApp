package ipvc.estg.fixity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import ipvc.estg.fixity.api.EndPoints
import ipvc.estg.fixity.api.Report
import ipvc.estg.fixity.api.ServiceBuilder
import ipvc.estg.fixity.viewModel.NoteListActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var reports: List<Report>
    private var userID: Int? = null
    private var problemID: String = ""
    private lateinit var problemDesc: TextView
    private lateinit var problemCategory: TextView
    private lateinit var latLng: TextView
    private lateinit var image: ImageView

    //ANIMATIONS
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        supportActionBar?.setTitle(R.string.reported_problems)

        //FAB BUTTONS
        var clicked = false
        val buttonReport = findViewById<FloatingActionButton>(R.id.button_reportProblem)
        val buttonShowOpt = findViewById<FloatingActionButton>(R.id.button_menuMap)
        val labelReport = findViewById<TextView>(R.id.labelReportProblem)


        //FAB BUTTONS SET ON CLICK LISTENER
        buttonShowOpt.setOnClickListener {
            if (!clicked) {
                buttonReport.visibility = View.VISIBLE
                buttonReport.startAnimation(fromBottom)
                labelReport.visibility = View.VISIBLE
                labelReport.startAnimation(fromBottom)
                buttonShowOpt.startAnimation(rotateOpen)
                buttonReport.isClickable = true
            } else {
                buttonReport.visibility = View.INVISIBLE
                buttonReport.startAnimation(toBottom)
                labelReport.visibility = View.INVISIBLE
                labelReport.startAnimation(toBottom)
                buttonShowOpt.startAnimation(rotateClose)
                buttonReport.isClickable = false
            }
            clicked = !clicked
        }

        buttonReport.setOnClickListener {
            //OPEN SOMETHING TO REPORT PROBLEM
            val intentReport = Intent(
                this@MapsActivity,
                ReportActivity::class.java
            )
            intentReport.putExtra(EXTRA_IDUSERLOGIN, userID)
            startActivity(intentReport)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //MOVE CAMERA TO VIANA DO CASTELO
        val viana = LatLng(41.6946, -8.83016)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(viana))
    }

    override fun onStart() {
        super.onStart()
        getPointsToMap()
    }

    private fun getPointsToMap() {
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getCoordinates()
        var position: LatLng

        val intent: Bundle? = intent.extras
        userID = intent?.getInt(MainActivity.EXTRA_USERID)

        call.enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                if (response.isSuccessful) {
                    reports = response.body()!!

                    mMap.clear()

                    for (report in reports) {

                        position = LatLng(report.latitude, report.longitude)

                        val items = resources.getStringArray(R.array.problemTypes)

                        var problemTypes = ""

                        when (report.problemType) {
                            1 -> {
                                problemTypes = items[0].toString()
                            }
                            2 -> {
                                problemTypes = items[1].toString()
                            }
                            3 -> {
                                problemTypes = items[2].toString()
                            }
                            4 -> {
                                problemTypes = items[3].toString()
                            }
                            5 -> {
                                problemTypes = items[4].toString()
                            }
                            6 -> {
                                problemTypes = items[5].toString()
                            }
                        }

                        // Setting a custom info window adapter for the google map
                        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                            // Use default InfoWindow frame
                            override fun getInfoWindow(arg0: Marker): View? {
                                return null
                            }

                            // Defines the contents of the InfoWindow
                            override fun getInfoContents(arg0: Marker): View {
                                var v: View? = null
                                try {

                                    // Getting view from the layout file info_window_layout
                                    v = layoutInflater.inflate(
                                        R.layout.custom_info_window,
                                        null
                                    )

                                    // Getting reference to the TextView to set latitude
                                    problemDesc =
                                        v!!.findViewById<View>(R.id.problemDesc) as TextView
                                    problemDesc.text = arg0.title

                                    problemCategory =
                                        v.findViewById<View>(R.id.problemCategory) as TextView
                                    problemCategory.text = arg0.snippet

                                    latLng =
                                        v.findViewById<View>(R.id.latLngProblem) as TextView
                                    latLng.text = arg0.position.toString()

                                    image =
                                        v.findViewById<View>(R.id.problemPic) as ImageView

                                    problemID = arg0.title.substringBefore(" -")

                                    Picasso.get()
                                        .load("https://fixity.pt/myslim/fixity/images/$problemID.jpeg")
                                        .into(image, MarkerCallback(arg0));


                                } catch (ev: Exception) {
                                    print(ev.message)
                                }
                                return v!!
                            }
                        })

                        mMap.setOnInfoWindowClickListener() {
                            val intentDetails = Intent(this@MapsActivity, ReportDetails::class.java)
                            intentDetails.putExtra(EXTRA_IDUSERLOGIN, userID)
                            intentDetails.putExtra(EXTRA_PROBLEMID, problemID)
                            intentDetails.putExtra(EXTRA_IDUSERREPORT, report.user_id)
                            intentDetails.putExtra(EXTRA_PROBLEMDESC, problemDesc.text.toString())
                            intentDetails.putExtra(
                                EXTRA_PROBELMCATEGORY,
                                problemCategory.text.toString()
                            )
                            intentDetails.putExtra(EXTRA_LATLNG, latLng.text.toString())
                            startActivity(intentDetails)
                        }

                        if (report.user_id == userID) {

                            mMap.addMarker(
                                MarkerOptions().position(position)
                                    .title("" + report.id + " - " + report.problem).snippet(
                                        problemTypes
                                    ).icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_AZURE
                                        )
                                    )
                            )
                        } else {
                            mMap.addMarker(
                                MarkerOptions().position(position)
                                    .title("" + report.id + " - " + report.problem).snippet(
                                        problemTypes
                                    )
                            )
                        }
                    }
                }

            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Toast.makeText(this@MapsActivity, R.string.error_register, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        menu.setGroupVisible(R.id.menuGroup, true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.notesAction -> {
                val intent = Intent(this, NoteListActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.accountAction -> {
                Toast.makeText(applicationContext, "click on account", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.logoutAction -> {
                //CALL SHARED PREFERENCES FILE
                val sharedPrefs: SharedPreferences =
                    getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE)

                val editor = sharedPrefs.edit()
                editor.clear().apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            R.id.settingAction -> {
                Toast.makeText(applicationContext, "click on settings", Toast.LENGTH_LONG).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Callback is an interface from Picasso:
    internal class MarkerCallback(marker: Marker?) : Callback<Any?>, com.squareup.picasso.Callback {
        var marker: Marker? = null

        init {
            this.marker = marker
        }

        override fun onSuccess() {
            if (marker == null) {
                return
            }
            if (!marker!!.isInfoWindowShown) {
                return
            }

            // If Info Window is showing, then refresh it's contents:
            marker!!.hideInfoWindow() // Calling only showInfoWindow() throws an error
            marker!!.showInfoWindow()
        }

        override fun onError(e: java.lang.Exception?) {
            TODO("Not yet implemented")
        }

        override fun onResponse(call: Call<Any?>, response: Response<Any?>) {
            TODO("Not yet implemented")
        }

        override fun onFailure(call: Call<Any?>, t: Throwable) {
            TODO("Not yet implemented")
        }
    }

    companion object {
        const val EXTRA_IDUSERLOGIN = "com.estg.fixity.messages.USERIDLOGIN"
        const val EXTRA_PROBLEMID = "com.estg.fixity.messages.PROBLEMID"
        const val EXTRA_IDUSERREPORT = "com.estg.fixity.messages.IDUSERREPORT"
        const val EXTRA_LATLNG = "com.estg.fixity.messages.LATLNG"
        const val EXTRA_PROBLEMDESC = "com.estg.fixity.messages.PROBLEMDESC"
        const val EXTRA_PROBELMCATEGORY = "com.estg.fixity.messages.PROBLEMCATEGORY"
    }

}