package uk.co.sullenart.teleport

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import uk.co.sullenart.teleport.settings.ProjectNameActivity

class MainActivity : BaseActivity(R.layout.activity_main) {

    @BindView(R.id.project_name)
    lateinit var projectName: TextView

    var locationService: LocationService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the location service
        Intent(this, LocationService::class.java).let {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // Show name setting screen if no name is already set
        namePreference.asObservable()
                .take(1)
                .filter { it.isEmpty() }
                .firstOrError()
                .subscribeBy(onSuccess = {
                    ProjectNameActivity.start(this)
                }, onError = {
                    Timber.d("Project name already set")
                })
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Timber.e("Error connecting to location service")
        }

        override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
            Timber.d("Connected to location service")
            locationService = (binder as LocationBinder).locationService
            monitorProjectName()
        }
    }

    private fun monitorProjectName() {
        namePreference.asObservable()
                .doOnNext { projectName.text = it }
                .filter { it.isNotEmpty() }
                .subscribeBy {
                    Timber.d("Project name set to $it")
                    locationService?.setProjectName(it)
                }
    }

    @OnClick(R.id.set_project_name)
    fun onSetName() {
        ProjectNameActivity.start(this)
    }

    @OnClick(R.id.quit)
    fun onQuit() {
        locationService?.stop()
        unbindService(serviceConnection)

        Intent(this, LocationService::class.java).let {
            stopService(it)
        }

        finish()
    }
}
