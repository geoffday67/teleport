package uk.co.sullenart.teleport

import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import uk.co.sullenart.teleport.settings.ProjectNameActivity

class MainActivity : BaseActivity(R.layout.activity_main) {

    @BindView(R.id.project_name)
    lateinit var projectName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        namePreference.asObservable()
                .doOnNext { projectName.text = it }
                .filter { it.isNotEmpty() }
                .subscribe { LocationService.start(this@MainActivity, it) }

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

    @OnClick(R.id.set_project_name)
    fun onSetName() {
        ProjectNameActivity.start(this)
    }
}
