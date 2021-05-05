package ipvc.estg.fixity

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipvc.estg.fixity.api.EndPoints
import ipvc.estg.fixity.api.OutputPost
import ipvc.estg.fixity.api.Report
import ipvc.estg.fixity.api.ServiceBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*

class UpdateProblemActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var selectedType: Int = 0
    private var fileToUpload: File? = null
    private var photo: Bitmap? = null
    private var requestFile: RequestBody? = null
    private var image: MultipartBody.Part? = null
    private val cameraRequest = 1888
    private val galleryRequest = 1001
    private val imageChoose = 1000
    private lateinit var updateProblem: Call<OutputPost>
    private lateinit var updateProblemPhoto: Call<OutputPost>
    private var isChanged: Boolean = false


    //PROBLEM TYPES
    // 1 - Accident
    // 2 - Water and sanitation
    // 3 - Public spaces
    // 4 - Street lighting
    // 5 - Mobility
    // 6 - Civil Protection


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

        //GET DATA FROM MAPS ACTIVITY
        val intent: Bundle? = intent.extras
        val problemId = intent?.getString(ReportDetails.EXTRA_IDPROBLEM)


        //TEXTFIELDS
        val buttonSave = findViewById<FloatingActionButton>(R.id.button_editProblem)
        val buttonCancel = findViewById<FloatingActionButton>(R.id.button_cancelEditProblem)
        val buttonCamera = findViewById<Button>(R.id.btn_uploadFromCamEdit)
        val buttonGallery = findViewById<Button>(R.id.btn_uploadFromGalleryEdit)
        imageView = findViewById(R.id.imageUploadEdit)
        val txtProblem = findViewById<EditText>(R.id.addProblemReportEdit)

        //EXPOSED MENU
        val textField = findViewById<AutoCompleteTextView>(R.id.txtCategoryEdit)
        val problemTypes = resources.getStringArray(R.array.problemTypes)

        //GET DATA FROM PROBLEM BY ID FROM SERVER
        val requestProblem = ServiceBuilder.buildService(EndPoints::class.java)
        val call = requestProblem.getProblemById(problemId)

        call.enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {

                    val op: Report = response.body()!!

                    selectedType = op.problemType

                    val items = resources.getStringArray(R.array.problemTypes)
                    var problemCategory = ""

                    when (op.problemType) {
                        1 -> {
                            problemCategory = items[0].toString()
                        }
                        2 -> {
                            problemCategory = items[1].toString()
                        }
                        3 -> {
                            problemCategory = items[2].toString()
                        }
                        4 -> {
                            problemCategory = items[3].toString()
                        }
                        5 -> {
                            problemCategory = items[4].toString()
                        }
                        6 -> {
                            problemCategory = items[5].toString()
                        }
                    }

                    val adapter =
                        ArrayAdapter(this@UpdateProblemActivity,
                            R.layout.exposed_menu_item,
                            problemTypes)
                    textField.setText(problemCategory)
                    textField.setAdapter(adapter)
                    textField.setOnItemClickListener { _, _, position, _ ->
                        selectedType = position + 1
                    }

                    txtProblem.setText(op.problem)

                    //GET IMAGE FROM SERVER
                    Glide.with(this@UpdateProblemActivity)
                        .load("https://fixity.pt/myslim/fixity/images/$problemId.jpeg")
                        .signature(ObjectKey(System.currentTimeMillis()))
                        .into(imageView)
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Toast.makeText(this@UpdateProblemActivity,
                    R.string.error_register,
                    Toast.LENGTH_SHORT).show()
            }

        })

        buttonCamera.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this@UpdateProblemActivity,
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
                    this@UpdateProblemActivity,
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
                    val intentGallery = Intent(Intent.ACTION_PICK)
                    intentGallery.type = "image/*"
                    startActivityForResult(intentGallery, imageChoose)
                }
            } else {
                val intentGallery = Intent(Intent.ACTION_PICK)
                intentGallery.type = "image/*"
                startActivityForResult(intentGallery, imageChoose)
            }
        }

        buttonCancel.setOnClickListener {
            finish()
        }

        buttonSave.setOnClickListener {
            if (txtProblem.text.isNullOrEmpty()) {
                Toast.makeText(
                    this@UpdateProblemActivity,
                    R.string.problem_required,
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                val request = ServiceBuilder.buildService(EndPoints::class.java)

                val problem: RequestBody = RequestBody.create(
                    MediaType.parse("multipart/form-data"),
                    txtProblem.text.toString()
                )

                val problemType: RequestBody = RequestBody.create(
                    MediaType.parse("multipart/form-data"),
                    selectedType.toString()
                )

                txtProblem.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {
                        Log.d("TAG", "beforeTextChanged")
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        if (txtProblem.isFocused) {
                            isChanged = true;
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        Log.d("TAG", "afterTextChanged")
                    }

                })

                if (photo != null) {

                    updateProblemPhoto = request.postEditProblemPhoto(
                        problemId, image
                    )

                    updateReportPhoto()
                } else {
                    if (isChanged) {
                        updateProblem = request.postEditProblem(
                            problemId, problem, problemType
                        )

                        updateReport()
                    } else {
                        Toast.makeText(this@UpdateProblemActivity,
                            R.string.nothing_changed,
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun updateReport() {
        //WEBSERVICE TO UPDATE PROBLEM
        updateProblem.enqueue(object : Callback<OutputPost> {
            override fun onResponse(
                call: Call<OutputPost>,
                response: Response<OutputPost>,
            ) {
                if (response.isSuccessful) {

                    val op: OutputPost = response.body()!!

                    if (!op.status) {
                        when (op.error) {
                            "data" -> Toast.makeText(
                                this@UpdateProblemActivity,
                                R.string.error_updating_data,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            "404" -> Toast.makeText(this@UpdateProblemActivity,
                                R.string.problemNotFound,
                                Toast.LENGTH_SHORT).show()
                            "empty" -> Toast.makeText(this@UpdateProblemActivity,
                                R.string.empty_fields,
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@UpdateProblemActivity,
                            R.string.data_updated_sucess,
                            Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<OutputPost>, t: Throwable) {
                Toast.makeText(
                    this@UpdateProblemActivity,
                    t.toString()/*R.string.error_register*/,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    fun updateReportPhoto() { //WEBSERVICE TO UPDATE PHOTO
        updateProblemPhoto.enqueue(object : Callback<OutputPost> {
            override fun onResponse(
                call: Call<OutputPost>,
                response: Response<OutputPost>,
            ) {
                if (response.isSuccessful) {

                    val op: OutputPost = response.body()!!

                    if (!op.status) {
                        when (op.error) {
                            "upload" -> Toast.makeText(
                                this@UpdateProblemActivity,
                                R.string.error_uploading,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            "size" -> Toast.makeText(
                                this@UpdateProblemActivity,
                                R.string.size_error,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            "uploading" -> Toast.makeText(
                                this@UpdateProblemActivity,
                                R.string.error_uploading,
                                Toast.LENGTH_SHORT
                            ).show()
                            "data" -> Toast.makeText(
                                this@UpdateProblemActivity,
                                R.string.error_updating_data,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            "404" -> Toast.makeText(this@UpdateProblemActivity,
                                R.string.problemNotFound,
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@UpdateProblemActivity,
                            R.string.data_updated_sucess,
                            Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<OutputPost>, t: Throwable) {
                Toast.makeText(
                    this@UpdateProblemActivity,
                    t.toString()/*R.string.error_register*/,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        })
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
}