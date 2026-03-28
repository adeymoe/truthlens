package uk.ac.tees.mad.e4615842.repository

import com.google.firebase.auth.FirebaseAuth
import uk.ac.tees.mad.e4615842.api.ApiService

class UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun firebaseRegister(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "User registered successfully")
                } else {
                    callback(false, task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun firebaseLogin(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Login successful")
                } else {
                    callback(false, task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}