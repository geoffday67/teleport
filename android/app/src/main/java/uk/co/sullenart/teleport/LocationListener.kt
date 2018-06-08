package uk.co.sullenart.teleport

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject

class LocationListener : ValueEventListener {
    val updates: PublishProcessor<LocationRequest> = PublishProcessor.create()

    init {
        FirebaseDatabase
                .getInstance()
                .getReference("/${Config.appName}/location").addValueEventListener(this)
    }

    override fun onCancelled(error: DatabaseError) {
        updates.onError(error.toException())
    }

    override fun onDataChange(data: DataSnapshot) {
        data.getValue(LocationRequest::class.java)?.let {
            updates.onNext(it)
        }
    }
}