/*
Show a notification that we're active and waiting for location updates.
Whenever there's an update from Firebase then:
    Store the latest position.
    Update mock provider with the position every N (2?) seconds.
    Show it in the notification.
 */

package uk.co.sullenart.teleport

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit

const val NOTIFICATION_CHANNEL_ID = "teleport"
const val NOTIFICATION_CHANNEL_NAME = "Location updates"
const val NOTIFICATION_ID = 1

class LocationService : Service() {
    val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mock_location)
            .setContentTitle("Teleport")
            .setContentText("Waiting for location update")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setChannelId(NOTIFICATION_CHANNEL_ID)

    lateinit var locationClient: FusedLocationProviderClient

    var latestRequest: LocationRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("Mock location service created")

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder?.build())
        Timber.d("Mock location service set to foreground")

        LocationListener().updates
                .mergeWith(
                        Flowable.interval(2, TimeUnit.SECONDS)
                                .map { latestRequest }
                )
                .retry(10)
                .subscribe {
                    it?.let {
                        Timber.d("Location request $it")
                        updateNotification(it.toString())
                        latestRequest = it
                    }
                }
    }

    fun updateNotification(content: String) {
        notificationBuilder?.let {
            it.setContentText(content)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, it.build())
        }
    }

    /*override fun onDataChange(data: DataSnapshot) {
        val request = data.getValue(LocationRequest::class.java)
        Timber.d("Location request ${request}")

        val location = Location("Teleport").apply {
            latitude = request?.latitude ?: 0.0
            longitude = request?.longitude ?: 0.0
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            accuracy = 5.0f
        }

        try {
            locationClient.setMockMode(true)
                    .addOnSuccessListener { Timber.d("Location client set to mock mode") }
            locationClient.setMockLocation(location)
                    .addOnSuccessListener { Timber.d("Mock location set to ${request?.latitude}, ${request?.longitude}") }
        } catch (e: SecurityException) {
            Timber.e(e)
        }
    }*/
}