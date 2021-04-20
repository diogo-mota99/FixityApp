package ipvc.estg.fixity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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


        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getCoordinates()
        var position: LatLng

        val intent: Bundle? = intent.extras
        val userID = intent?.getInt(MainActivity.EXTRA_USERID)

        call.enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                if (response.isSuccessful) {
                    reports = response.body()!!

                    Toast.makeText(this@MapsActivity, "$userID", Toast.LENGTH_SHORT).show()

                    for (report in reports) {

                        position = LatLng(report.latitude, report.longitude)

                        if (report.user_id == userID) {
                            mMap.addMarker(
                                MarkerOptions().position(position).title(report.problem).icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_AZURE
                                    )
                                )
                            )
                        } else {
                            mMap.addMarker(
                                MarkerOptions().position(position).title(report.problem)
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //MOVE CAMERA TO VIANA DO CASTELO
        val viana = LatLng(41.6946, -8.83016)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(viana))
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
                return true
            }
            R.id.settingAction -> {
                Toast.makeText(applicationContext, "click on settings", Toast.LENGTH_LONG).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}