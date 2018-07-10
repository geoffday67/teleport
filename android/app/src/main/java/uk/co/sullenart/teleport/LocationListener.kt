package uk.co.sullenart.teleport

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import uk.co.sullenart.teleport.model.LocationRequest

class LocationListener {
    private val updateProcessor: PublishProcessor<LocationRequest> = PublishProcessor.create()

    val updates: Flowable<LocationRequest>
        get() = updateProcessor.share()

    fun setProjectName(name: String) {
        FirebaseDatabase
                .getInstance()
                .getReference("/$name/location").addValueEventListener(
                        object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                updateProcessor.onError(error.toException())
                            }

                            override fun onDataChange(data: DataSnapshot) {
                                data.getValue(LocationRequest::class.java)?.let {
                                    Timber.d("Location request $it")
                                    updateProcessor.onNext(it)
                                }
                            }
                        }
                )
    }
}