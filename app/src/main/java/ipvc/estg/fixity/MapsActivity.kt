package ipvc.estg.fixity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import ipvc.estg.fixity.adapters.CategoryAdapter
import ipvc.estg.fixity.api.Category
import ipvc.estg.fixity.api.EndPoints
import ipvc.estg.fixity.api.Report
import ipvc.estg.fixity.api.ServiceBuilder
import ipvc.estg.fixity.viewModel.NoteListActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, CategoryAdapter.CategoryRecycler {

    private lateinit var mMap: GoogleMap
    private lateinit var reports: List<Report>
    private var userID: Int? = null
    private var problemID: String = ""
    private var problemTimestamp: String = ""
    private lateinit var problemDesc: TextView
    private lateinit var problemCategory: TextView
    private lateinit var latLng: TextView
    private lateinit var image: ImageView
    private var userLocation: LatLng? = null
    private lateinit var radiusSlider: Slider
    private var radius: Float? = 0f
    private var circle: Circle? = null
    private lateinit var listView: RecyclerView
    private var filterCategory: ArrayList<Int> = ArrayList()
    private var filterDis = false
    private var filterCat = false

    //LOCATION
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var lastLocation: Location

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

        //LOCATION
        createLocationRequest()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                userLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
            }
        }


        //FAB BUTTONS
        var clicked = false
        val buttonReport = findViewById<FloatingActionButton>(R.id.button_reportProblem)
        val buttonShowOpt = findViewById<FloatingActionButton>(R.id.button_menuMap)
        val buttonFilter = findViewById<FloatingActionButton>(R.id.button_filterProblem)
        val cardReportProblem = findViewById<CardView>(R.id.cardReportProblem)
        val cardFilterProblem = findViewById<CardView>(R.id.cardFilterProblem)
        val cardFilter = findViewById<CardView>(R.id.cardFilter)
        val iconClose = findViewById<ImageView>(R.id.closeFilter)
        val radiusMeters = findViewById<TextView>(R.id.radius)

        //CHECKBOX DISABLE DISTANCE FILTER
        val chkDisable = findViewById<CheckBox>(R.id.checkDisable)

        //RADIUS SLIDER
        radiusSlider = findViewById(R.id.radiusSlider)

        radiusSlider.addOnChangeListener { _, value, _ ->
            radius = value
            radiusMeters.text = (radius!! / 1000).toString().plus(" km")
        }

        radiusSlider.setLabelFormatter { value: Float ->
            val format = (value / 1000).toString() + " km"
            format.format(value.toDouble())
        }

        radiusSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                Log.d("TAG", "onStartTrackingTouch")
                circle?.remove()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                getPointsToMap(filterDis, filterCat, userLocation)
            }

        })

        //RECYCLERVIEW
        listView = findViewById(R.id.recyclerCategory)
        listView.setHasFixedSize(true)
        listView.layoutManager = LinearLayoutManager(applicationContext)

        val problemTypes = resources.getStringArray(R.array.problemTypes)
        val list: ArrayList<Category> = ArrayList()

        for (categories in problemTypes) {
            val category = Category(categories, false)
            list.add(category)
        }

        val adapter = CategoryAdapter(this, this)
        listView.adapter = adapter

        adapter.setCategory(list)

        chkDisable.setOnClickListener {
            if (chkDisable.isChecked) {
                radiusSlider.visibility = View.INVISIBLE
                filterDis = false
                filterCat = filterCategory.size > 0
            } else {
                radiusSlider.visibility = View.VISIBLE
                filterDis = true
                filterCat = filterCategory.size > 0
            }
            getPointsToMap(filterDis, filterCat, userLocation)
        }


        //FAB BUTTONS SET ON CLICK LISTENER
        buttonShowOpt.setOnClickListener {
            if (!clicked) {
                buttonReport.visibility = View.VISIBLE
                buttonReport.startAnimation(fromBottom)
                buttonFilter.visibility = View.VISIBLE
                buttonFilter.startAnimation(fromBottom)
                cardReportProblem.visibility = View.VISIBLE
                cardReportProblem.startAnimation(fromBottom)
                cardFilterProblem.visibility = View.VISIBLE
                cardFilterProblem.startAnimation(fromBottom)
                buttonShowOpt.startAnimation(rotateOpen)
                buttonFilter.isClickable = true
                buttonReport.isClickable = true
            } else {
                buttonReport.visibility = View.INVISIBLE
                buttonReport.startAnimation(toBottom)
                buttonFilter.visibility = View.INVISIBLE
                buttonFilter.startAnimation(toBottom)
                cardReportProblem.visibility = View.INVISIBLE
                cardReportProblem.startAnimation(toBottom)
                cardFilterProblem.visibility = View.INVISIBLE
                cardFilterProblem.startAnimation(toBottom)
                buttonShowOpt.startAnimation(rotateClose)
                buttonFilter.isClickable = false
                buttonReport.isClickable = false
            }
            clicked = !clicked
        }

        buttonReport.setOnClickListener {
            //OPEN SCREEN TO REPORT PROBLEM
            val intentReport = Intent(
                this@MapsActivity,
                ReportActivity::class.java
            )
            intentReport.putExtra(EXTRA_IDUSERLOGIN, userID)
            startActivity(intentReport)
        }

        buttonFilter.setOnClickListener {
            //SHOW FILTER POPUP
            cardFilter.visibility = View.VISIBLE

            //RESET VALUE OF SLIDER
            radiusSlider.value = 0.0f
            radiusMeters.text = (radius!! / 1000).toString().plus(" km")

            filterDis = true

            //CALL MAPS POINTS WITH INITIAL FILTER
            getPointsToMap(filterDis, filterCat, userLocation)

            buttonFilter.isClickable = false

        }

        iconClose.setOnClickListener {
            //HIDE FILTER POPUP
            cardFilter.visibility = View.INVISIBLE
            circle?.remove()
            chkDisable.isChecked = false
            filterCategory.clear()
            adapter.resetCheckbox(true)

            //CALL ALL MAP MARKERS AGAIN
            getPointsToMap(false, false, null)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //MOVE CAMERA TO VIANA DO CASTELO
        val viana = LatLng(41.6946, -8.83016)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viana, 8f))
    }

    private fun calculateDistance(
        latUser: Double,
        lngUser: Double,
        latReport: Double,
        lngReport: Double,
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(latUser, lngUser, latReport, lngReport, results)
        //distance in meters
        return results[0]
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MapsActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000 //5 em 5 segundos
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        getPointsToMap(filterDistance = false, filterCategoryMap = false, userLastLocation = null)
        startLocationUpdates()
    }

    private fun getPointsToMap(
        filterDistance: Boolean,
        filterCategoryMap: Boolean,
        userLastLocation: LatLng?,
    ) {
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

                                    problemDesc =
                                        v!!.findViewById<View>(R.id.problemDesc) as TextView
                                    problemDesc.text = arg0.title

                                    problemCategory =
                                        v.findViewById<View>(R.id.problemCategory) as TextView
                                    problemCategory.text = arg0.snippet.substringBefore(" -")

                                    latLng =
                                        v.findViewById<View>(R.id.latLngProblem) as TextView
                                    latLng.text = arg0.position.toString()

                                    image =
                                        v.findViewById<View>(R.id.problemPic) as ImageView

                                    problemID = arg0.title.substringBefore(" -")

                                    problemTimestamp = arg0.snippet.substringAfter("- ")

                                    Glide.with(this@MapsActivity)
                                        .load("https://fixity.pt/myslim/fixity/images/$problemID.jpeg")
                                        .signature(ObjectKey(problemTimestamp))
                                        .into(image)

                                } catch (ev: Exception) {
                                    print(ev.message)
                                }
                                return v!!
                            }
                        })

                        mMap.setOnInfoWindowClickListener {
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

                        mMap.setOnMarkerClickListener { mark ->

                            mark.showInfoWindow()

                            val handler = Handler()
                            handler.postDelayed({ mark.showInfoWindow() }, 400)

                            true
                        }

                        if (filterCategoryMap && filterDistance) {
                            //CIRCLE OPTIONS
                            val circleOpt: CircleOptions =
                                CircleOptions().center(userLocation)
                                    .radius(radius?.toDouble()!!) //IN METERS
                            //DRAW CIRCLE ON MAP
                            circle = mMap.addCircle(circleOpt)
                            circle?.strokeColor = Color.RED
                            circle?.fillColor = Color.parseColor("#2087CEFA")

                            for (cat in filterCategory) {
                                if (calculateDistance(userLastLocation!!.latitude,
                                        userLastLocation.longitude,
                                        report.latitude,
                                        report.longitude) <= radius!! && cat == report.problemType
                                ) {
                                    if (report.user_id == userID) {


                                        mMap.addMarker(
                                            MarkerOptions().position(position)
                                                .title("" + report.id + " - " + report.problem)
                                                .snippet(
                                                    "" + problemTypes + " - " + report.timestamp
                                                ).icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_AZURE
                                                    )
                                                )
                                        )
                                    } else {
                                        mMap.addMarker(
                                            MarkerOptions().position(position)
                                                .title("" + report.id + " - " + report.problem)
                                                .snippet(
                                                    problemTypes
                                                )
                                        )
                                    }
                                }
                            }
                        } else if (filterDistance) {

                            //CIRCLE OPTIONS
                            val circleOpt: CircleOptions =
                                CircleOptions().center(userLocation)
                                    .radius(radius?.toDouble()!!) //IN METERS
                            //DRAW CIRCLE ON MAP
                            circle = mMap.addCircle(circleOpt)
                            circle?.strokeColor = Color.RED
                            circle?.fillColor = Color.parseColor("#2087CEFA")

                            if (calculateDistance(userLastLocation!!.latitude,
                                    userLastLocation.longitude,
                                    report.latitude,
                                    report.longitude) <= radius!!
                            ) {


                                if (report.user_id == userID) {


                                    mMap.addMarker(
                                        MarkerOptions().position(position)
                                            .title("" + report.id + " - " + report.problem).snippet(
                                                "" + problemTypes + " - " + report.timestamp
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
                        } else if (filterCategoryMap) {
                            for (cat in filterCategory) {
                                if (cat == report.problemType) {
                                    if (report.user_id == userID) {


                                        mMap.addMarker(
                                            MarkerOptions().position(position)
                                                .title("" + report.id + " - " + report.problem)
                                                .snippet(
                                                    "" + problemTypes + " - " + report.timestamp
                                                ).icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_AZURE
                                                    )
                                                )
                                        )
                                    } else {
                                        mMap.addMarker(
                                            MarkerOptions().position(position)
                                                .title("" + report.id + " - " + report.problem)
                                                .snippet(
                                                    problemTypes
                                                )
                                        )
                                    }
                                }
                            }
                        } else {
                            if (report.user_id == userID) {


                                mMap.addMarker(
                                    MarkerOptions().position(position)
                                        .title("" + report.id + " - " + report.problem)
                                        .snippet(
                                            "" + problemTypes + " - " + report.timestamp
                                        ).icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_AZURE
                                            )
                                        )
                                )
                            } else {
                                mMap.addMarker(
                                    MarkerOptions().position(position)
                                        .title("" + report.id + " - " + report.problem)
                                        .snippet(
                                            problemTypes
                                        )
                                )
                            }
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

    companion object {
        const val EXTRA_IDUSERLOGIN = "com.estg.fixity.messages.USERIDLOGIN"
        const val EXTRA_PROBLEMID = "com.estg.fixity.messages.PROBLEMID"
        const val EXTRA_IDUSERREPORT = "com.estg.fixity.messages.IDUSERREPORT"
        const val EXTRA_LATLNG = "com.estg.fixity.messages.LATLNG"
        const val EXTRA_PROBLEMDESC = "com.estg.fixity.messages.PROBLEMDESC"
        const val EXTRA_PROBELMCATEGORY = "com.estg.fixity.messages.PROBLEMCATEGORY"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1


    }

    override fun updateCategoryFilter(category: ArrayList<Int>) {
        filterCategory = category
        filterCat = filterCategory.size > 0
        getPointsToMap(filterDis, filterCat, userLocation)
    }

}