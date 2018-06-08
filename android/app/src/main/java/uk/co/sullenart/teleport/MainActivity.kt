package uk.co.sullenart.teleport

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Intent(this, LocationService::class.java).let {
            startService(it)
        }

        /*val client = LocationServices.getFusedLocationProviderClient(this)

        val database = FirebaseDatabase.getInstance()
        database.getReference("/location").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                val request = data.getValue(LocationRequest::class.java)
                Timber.d("Location request ${request.toString()}")

                val location = Location("blah").apply {
                    latitude = request?.latitude ?: 0.0
                    longitude = request?.longitude ?: 0.0
                    time = System.currentTimeMillis()
                    elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                    accuracy = 10.0f
                }

                try {
                    client.setMockMode(true)
                            .addOnSuccessListener { Timber.d("Location client set to mock mode") }
                    client.setMockLocation(location)
                            .addOnSuccessListener { Timber.d("Mock location set to ${request?.latitude}, ${request?.longitude}") }
                } catch (e: SecurityException) {
                    Timber.e(e)
                }
            }
        })*/
    }


}
