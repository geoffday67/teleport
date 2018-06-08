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
    }
}
