package ipvc.estg.fixity

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipvc.estg.fixity.api.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.*
import java.util.*


class
ReportActivity : AppCompatActivity() {

    private val cameraRequest = 1888
    private val galleryRequest = 1001
    private val imageChoose = 1000
    private var photo: Bitmap? = null
    private var fileToUpload: File? = null
    private var image: MultipartBody.Part? = null
    private var requestFile: RequestBody? = null
    private lateinit var imageView: ImageView
    private var selectedType: Int? = 1
    var longitude: Double? = null
    var latitude: Double? = null
    private var idUser: Int? = null

    //LOCATION
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var lastLocation: Location

    //PROBLEM TYPES
    // 1 - Accident
    // 2 - Water and sanitation
    // 3 - Public spaces
    // 4 - Street lighting
    // 5 - Mobility
    // 6 - Civil Protection


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = supportActionBar

        if (actionBar != null) {

            // Customize the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

            // showing the back button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true)

            // Set toolbar title/app title
            actionBar.setTitle(R.string.report_problem)
        }

        //GET USER ID FROM MAPS ACTIVITY
        val intentUser: Bundle? = intent.extras
        idUser = intentUser?.getInt(MapsActivity.EXTRA_IDUSERLOGIN)


        //LOCATION
        createLocationRequest()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                latitude = latLng.latitude
                longitude = latLng.longitude
            }
        }

        val buttonSave = findViewById<FloatingActionButton>(R.id.button_addProblem)
        val buttonCancel = findViewById<FloatingActionButton>(R.id.button_cancelAddProblem)
        val buttonCamera = findViewById<Button>(R.id.btn_uploadFromCam)
        val buttonGallery = findViewById<Button>(R.id.btn_uploadFromGallery)
        imageView = findViewById(R.id.imageUpload)
        val txtProblem = findViewById<EditText>(R.id.addProblemReport)


        val textField = findViewById<AutoCompleteTextView>(R.id.txtCategory)
        val problemTypes = resources.getStringArray(R.array.problemTypes)


        if (textField != null) {
            val adapter =
                ArrayAdapter(this@ReportActivity, R.layout.exposed_menu_item, problemTypes)
            textField.setText(problemTypes[0])
            textField.setAdapter(adapter)
            textField.setOnItemClickListener { _, _, position, _ ->
                selectedType = position + 1
            }

            buttonCamera.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this@ReportActivity,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        cameraRequest
                    )
                }

                if (ContextCompat.checkSelfPermission(
                        this@ReportActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, cameraRequest)
                }
            }

            buttonGallery.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, galleryRequest)
                    } else {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, imageChoose)
                    }
                } else {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, imageChoose)
                }
            }

            buttonCancel.setOnClickListener {
                finish()
            }

            buttonSave.setOnClickListener {
                if (photo != null) {
                    if (txtProblem.text.isNullOrEmpty()) {
                        Toast.makeText(
                            this@ReportActivity,
                            R.string.problem_required,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        val request =
                            ServiceBuilder.buildService(EndPoints::class.java)

                        val problem: RequestBody = RequestBody.create(
                            MediaType.parse("multipart/form-data"),
                            txtProblem.text.toString()
                        )
                        val latitude: RequestBody = RequestBody.create(
                            MediaType.parse("multipart/form-data"),
                            latitude.toString()
                        )
                        val longitude: RequestBody = RequestBody.create(
                            MediaType.parse("multipart/form-data"),
                            longitude.toString()
                        )
                        val problemType: RequestBody = RequestBody.create(
                            MediaType.parse("multipart/form-data"),
                            selectedType.toString()
                        )
                        val userID: RequestBody = RequestBody.create(
                            MediaType.parse("multipart/form-data"),
                            idUser.toString()
                        )


                        val reportProblem = request.postReportProblem(
                            problem, latitude, longitude, image, problemType, userID
                        )

                        reportProblem.enqueue(object : retrofit2.Callback<OutputPost> {
                            override fun onResponse(
                                call: Call<OutputPost>,
                                response: Response<OutputPost>,
                            ) {
                                if (response.isSuccessful) {

                                    val op: OutputPost = response.body()!!

                                    if (!op.status) {
                                        when (op.error) {
                                            "upload" -> Toast.makeText(
                                                this@ReportActivity,
                                                R.string.error_uploading,
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            "size" -> Toast.makeText(
                                                this@ReportActivity,
                                                R.string.size_error,
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            "uploading" -> Toast.makeText(
                                                this@ReportActivity,
                                                R.string.error_uploading,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            "updating" -> Toast.makeText(
                                                this@ReportActivity,
                                                R.string.error_updating,
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }
                                    } else {
                                        finish()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<OutputPost>, t: Throwable) {
                                Toast.makeText(
                                    this@ReportActivity,
                                    R.string.error_register,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()

                            }
                        })
                    }
                } else {
                    Toast.makeText(this@ReportActivity, R.string.photo_required, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this@ReportActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@ReportActivity,
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
        startLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == cameraRequest) {
                if (data != null) {
                    photo = data.extras?.get("data") as Bitmap
                    val uri = bitmapToFile(photo!!)
                    imageView.setImageURI(uri)

                    requestFile =
                        RequestBody.create(
                            MediaType.parse("multipart/form-data"),
                            fileToUpload!!
                        )

                    // MultipartBody.Part is used to send also the actual file name
                    image =
                        MultipartBody.Part.createFormData(
                            "image",
                            fileToUpload!!.name,
                            requestFile!!
                        )
                }

            }

            if (requestCode == imageChoose) {
                if (data != null) {
                    val inputStream: InputStream? = contentResolver.openInputStream(data.data!!)
                    photo = BitmapFactory.decodeStream(inputStream)
                    val uri = bitmapToFile(photo!!)
                    imageView.setImageURI(uri)

                    requestFile =
                        RequestBody.create(
                            MediaType.parse("multipart/form-data"),
                            fileToUpload!!
                        )

                    // MultipartBody.Part is used to send also the actual file name
                    image =
                        MultipartBody.Part.createFormData(
                            "image",
                            fileToUpload!!.name,
                            requestFile!!
                        )
                }
            }
        }
    }

    //SAVE BITMAP TO FILE
    private fun bitmapToFile(bitmap: Bitmap): Uri {
        //GET CONTEXT WRAPPER
        val wrapper = ContextWrapper(applicationContext)

        //Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        fileToUpload = file

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}