package com.example.chat_chat.googleSign

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.chat_chat.ChatViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,  //oneTapClient: An instance of SignInClient which handles the Google One Tap API calls.
    val viewModel: ChatViewModel
) {
    private val auth = Firebase.auth  // Initializes a Firebase Authentication instance. This instance is later used to sign in and sign out users.

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }



    suspend fun signInWithIntent(intent: Intent): SignInResult {
        viewModel.resetState()
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName.toString(),
                        profilePictureUrl = photoUrl.toString()
                            .substring(0, photoUrl.toString().length - 6),
                        email = email.toString()

                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("228505499393-99084sagnqqdb87j31pc0daj4vamgv8j.apps.googleusercontent.com")
                    .build()
            ).setAutoSelectEnabled(true).build()
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName.toString(),
            profilePictureUrl = photoUrl.toString().substring(0, photoUrl.toString().length - 6),
            email = email.toString()
        )
    }
}

