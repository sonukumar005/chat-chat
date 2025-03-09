package com.example.chat_chat.googleSign

import com.example.chat_chat.ChatViewModel

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?,
    val email: String?
)

data class AppState(
    val isSignedIn:Boolean = false,
    val userData: UserData? = null,
    val signInError: String? = null
)


