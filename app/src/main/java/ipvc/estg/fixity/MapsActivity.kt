package ipvc.estg.fixity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ipvc.estg.fixity.api.Report
import ipvc.estg.fixity.api.EndPoints
import ipvc.estg.fixity.api.ServiceBuilder
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

        call.enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                if (response.isSuccessful) {
                    reports = response.body()!!

                    for (report in reports) {
                        position = LatLng(report.latitude, report.longitude)
                        mMap.addMarker(MarkerOptions().position(position).title("teste"))
                    }
                }

            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Toast.makeText(this@MapsActivity, R.string.error_register, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //MOVE CAMERA TO VIANA DO CASTELO
        val viana = LatLng(41.6946, -8.83016)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(viana))
    }
}