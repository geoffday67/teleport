package uk.co.sullenart.teleport.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Completable
import io.reactivex.Single
import uk.co.sullenart.teleport.model.LocationRequest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Database
@Inject
constructor() {
    private val database = FirebaseDatabase.getInstance()

    fun isProjectNameAvailable(name: String): Single<Boolean> =
            Single.create { emitter ->
                database.getReference("/$name").addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                emitter.onError(error.toException())
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                emitter.onSuccess(!snapshot.exists())
                            }
                        }
                )
            }

    fun writeEmptyProject(name: String): Completable =
            Completable.create { emitter ->
                database.getReference("/$name/location").setValue(LocationRequest.createInvalid())
                        .addOnCompleteListener { emitter.onComplete() }
                        .addOnFailureListener { emitter.onError(it) }
            }

    fun checkNameIsAvailable(name: String): Completable {
        return isProjectNameAvailable(name)
                .flatMapCompletable {
                    if (it)
                        Completable.complete() else
                        Completable.error(Exception("Name is not available"))
                }
    }

    fun checkNameExists(name: String): Completable =
            isProjectNameAvailable(name)
                    .flatMapCompletable {
                        if (!it)
                            Completable.complete() else
                            Completable.error(Exception("Project doesn't exist"))
                    }
}