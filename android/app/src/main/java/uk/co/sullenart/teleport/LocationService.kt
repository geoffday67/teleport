package uk.co.sullenart.teleport

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.Flowable
import timber.log.Timber
import uk.co.sullenart.teleport.model.LocationRequest
import java.util.concurrent.TimeUnit

const val NOTIFICATION_CHANNEL_ID = "teleport"
const val NOTIFICATION_CHANNEL_NAME = "Location updates"
const val NOTIFICATION_ID = 1

class LocationService : Service() {
    companion object {
        const val EXTRA_PROJECT_NAME = "project-name"

        fun start(context: Context, projectName: String) {
            Intent(context, LocationService::class.java).let {
                it.putExtra(EXTRA_PROJECT_NAME, projectName)
                context.startService(it)
            }
        }
    }

    val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mock_location)
            .setContentTitle("Teleport")
            .setContentText("Waiting for location update")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setChannelId(NOTIFICATION_CHANNEL_ID)

    lateinit var notificationManager: NotificationManager
    lateinit var locationClient: FusedLocationProviderClient

    val locationListener = LocationListener()
    var latestRequest = LocationRequest.createInvalid()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("Mock location service created")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        Timber.d("Mock location service set to foreground")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.getStringExtra(EXTRA_PROJECT_NAME)?.let {
            locationListener.setProjectName(it)
            locationListener.updates
                    .mergeWith(
                            Flowable.interval(2, TimeUnit.SECONDS)
                                    .map { latestRequest }
                    )
                    .filter { it.isValid() }
                    .retry(10)
                    .subscribe {
                        updateNotification(it.toString())
                        updateMock(it)
                        latestRequest = it
                    }
        }

        return START_REDELIVER_INTENT
    }

    fun updateNotification(content: String) {
        notificationBuilder.let {
            it.setContentText(content)
            notificationManager.notify(NOTIFICATION_ID, it.build())
        }
    }

    fun updateMock(request: LocationRequest) {
        val location = Location("Teleport").apply {
            latitude = request.latitude
            longitude = request.longitude
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            accuracy = 5.0f
        }

        try {
            locationClient.setMockMode(true)
                    .addOnFailureListener { Timber.e(it) }
            locationClient.setMockLocation(location)
                    .addOnFailureListener { Timber.e(it) }
        } catch (e: SecurityException) {
            Timber.e(e)
        }
    }
}