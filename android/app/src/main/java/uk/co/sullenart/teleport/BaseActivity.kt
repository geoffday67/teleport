package uk.co.sullenart.teleport

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import butterknife.ButterKnife
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.disposables.CompositeDisposable
import uk.co.sullenart.teleport.data.Database
import javax.inject.Inject
import javax.inject.Named

@SuppressLint("Registered")
open class BaseActivity(@LayoutRes val layout: Int) : AppCompatActivity() {
    @Inject
    protected lateinit var database: Database

    @Inject
    @field:Named("namePreference")
    protected lateinit var namePreference: Preference<String>

    protected val compositeDisposable = CompositeDisposable()
    protected lateinit var rootView: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = LayoutInflater.from(this).inflate(layout, null) as ViewGroup
        setContentView(rootView)
        ButterKnife.bind(this)
        (application as MainApplication).component.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.clear()
    }

    protected fun showMessage(message: String) {
        Toast.makeText(this, message, LENGTH_LONG).show()
    }
}