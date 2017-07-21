package co.esclub.searchnshop.model.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Created by tae.kim on 16/07/2017.
 */

object FirebaseService {

    val auth = FirebaseAuth.getInstance()
    var isSignIn = false

    fun user(): FirebaseUser? {
        return auth.currentUser
    }

    fun signIn(listener: (isSignIn: Boolean) -> Unit) {
        auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isSignIn = true
                    }
                    listener.invoke(isSignIn)
                }
    }
}