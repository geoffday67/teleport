package uk.co.sullenart.teleport

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import uk.co.sullenart.teleport.model.LocationRequest
import java.util.concurrent.TimeUnit

const val NOTIFICATION_CHANNEL_ID = "teleport"
const val NOTIFICATION_CHANNEL_NAME = "Location updates"
const val NOTIFICATION_ID = 1

class LocationService : Service() {

    override fun onBind(intent: Intent?): IBinder =
            LocationBinder(this)

    lateinit var notificationManager: NotificationManager
    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var locationClient: FusedLocationProviderClient

    val locationListener = LocationListener()
    var latestRequest = LocationRequest.createInvalid()
    var locationDisposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("Mock location service created")

        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mock_location)
                .setContentTitle("Teleport")
                .setContentText("Waiting for location update")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentIntent(PendingIntent.getActivity(this, 1,
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }, 0))

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        Timber.d("Mock location service set to foreground")
    }

    override fun onDestroy() {
        super.onDestroy()

        locationDisposable?.dispose()
    }

    fun setProjectName(projectName: String) {
        locationListener.setProjectName(projectName)

        locationDisposable?.dispose()
        locationDisposable = locationListener.updates
                .mergeWith(flowableLatest)
                .filter { it.isValid() }
                .retry(10)
                .subscribe {
                    updateNotification(it.toString())
                    updateMock(it)
                    latestRequest = it
                }
    }

    private val flowableLatest =
            Flowable.interval(2, TimeUnit.SECONDS)
                    .map { latestRequest }

    private fun updateNotification(content: String) {
        notificationBuilder.let {
            it.setContentText(content)
            notificationManager.notify(NOTIFICATION_ID, it.build())
        }
    }

    private fun updateMock(request: LocationRequest) {
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

data class LocationBinder(
        val locationService: LocationService
) : Binder()