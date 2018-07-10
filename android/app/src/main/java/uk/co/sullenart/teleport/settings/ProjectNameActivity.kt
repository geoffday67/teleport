package uk.co.sullenart.teleport.settings

import android.content.Context
import android.content.Intent
import android.support.design.widget.TextInputEditText
import android.view.View
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.OnClick
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import uk.co.sullenart.teleport.BaseActivity
import uk.co.sullenart.teleport.R

class ProjectNameActivity : BaseActivity(R.layout.activity_project_name) {

    companion object {
        fun start(context: Context) {
            Intent(context, ProjectNameActivity::class.java).let {
                context.startActivity(it)
            }
        }
    }

    @BindView(R.id.progress)
    lateinit var progressBar: ProgressBar

    @BindView(R.id.project_name)
    lateinit var projectName: TextInputEditText

    @OnClick(R.id.save)
    fun onSave() {
        val name = projectName.text.toString()
        compositeDisposable.add(
                database.checkNameExists(name)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { showProgress() }
                        .doOnTerminate { hideProgress() }
                        .subscribeBy(onComplete = {
                            namePreference.set(name)
                            finish()
                        }, onError = { showMessage(it.message ?: "Unknown error") })
        )
    }

    @OnClick(R.id.new_project)
    fun onNew() {
        val name = projectName.text.toString()
        compositeDisposable.add(
                database.checkNameIsAvailable(name)
                        .andThen(database.writeEmptyProject(name))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { showProgress() }
                        .doOnTerminate { hideProgress() }
                        .subscribeBy(onComplete = {
                            namePreference.set(name)
                            finish()
                        }, onError = { showMessage(it.message ?: "Unknown error") })
        )
    }

    @OnClick(R.id.cancel)
    fun onCancel() {
        finish()
    }

    private fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progressBar.visibility = View.INVISIBLE
    }
}