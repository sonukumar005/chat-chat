package com.example.chat_chat

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chat_chat.googleSign.AppState
import com.example.chat_chat.googleSign.SignInResult
import com.example.chat_chat.googleSign.UserData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private val userCollection = Firebase.firestore.collection(USER_COLLECTION)
    fun resetState() {
    }

    fun onSignInResult(signInResult: SignInResult) {
        _state.update {
            it.copy(
                isSignedIn = signInResult.data != null,
//                userData = signInResult.data,
                signInError = signInResult.errorMessage
            )
        }
    }
    fun addUserToFirestore(userData: UserData) {
        val userDataMap = mapOf(
            "userId" to userData?.userId,
            "username" to userData?.username,
            "profilePictureUrl" to userData?.profilePictureUrl,
            "email" to userData?.email
        )
        val userDocument = userCollection.document(userData.userId)
        userDocument.get().addOnSuccessListener {
            if (it.exists()) {
                userDocument.update(userDataMap).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User data updated to Firebase  successfully")
                }.addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User data updated to Firebase failed")
                }
            } else {
                userDocument.set(userDataMap).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User data added to Firebase successfully")
                }.addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User data added to Firebase failed")
                }
            }
        }
    }
}