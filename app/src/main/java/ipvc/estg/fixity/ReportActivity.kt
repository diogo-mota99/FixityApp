package ipvc.estg.fixity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ReportActivity : AppCompatActivity() {

    private val cameraRequest = 1888
    private val galleryRequest = 1001;
    private val imageChoose = 1000;
    private var imageUri: Uri? = null

    lateinit var imageView: ImageView


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

        val buttonSave = findViewById<FloatingActionButton>(R.id.button_addProblem)
        val buttonCancel = findViewById<FloatingActionButton>(R.id.button_cancelAddProblem)
        val buttonCamera = findViewById<Button>(R.id.btn_uploadFromCam)
        val buttonGallery = findViewById<Button>(R.id.btn_uploadFromGallery)
        imageView = findViewById(R.id.imageUpload)
        val txtProblem = findViewById<EditText>(R.id.addProblemReport)


        buttonCamera.setOnClickListener {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@ReportActivity,
                    Manifest.permission.CAMERA
                )
            ) {
                val builder = AlertDialog.Builder(this@ReportActivity)
                builder.setPositiveButton(R.string.ok) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        cameraRequest
                    )
                }
                builder.setNegativeButton(R.string.cancel) { _, _ ->
                }
                builder.setTitle("Permission needed")
                builder.setMessage("This permission is needed to report problems!")
                builder.create().show()


            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    cameraRequest
                )
            }

            if(ContextCompat.checkSelfPermission(this@ReportActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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
            //Toast.makeText(this, txtProblem.text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == cameraRequest) {
                val photo: Bitmap = data?.extras?.get("data") as Bitmap
                imageView.setImageBitmap(photo)
            }
            if (requestCode == imageChoose) {
                imageUri = data?.data
                imageView.setImageURI(imageUri)
            }
        }
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