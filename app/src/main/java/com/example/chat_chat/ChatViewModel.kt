package com.example.chat_chat

import android.content.ContentValues
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.FirstBaseline
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chat_chat.googleSign.AppState
import com.example.chat_chat.googleSign.ChatData
import com.example.chat_chat.googleSign.ChatUserData
import com.example.chat_chat.googleSign.Message
import com.example.chat_chat.googleSign.SignInResult
import com.example.chat_chat.googleSign.UserData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private val userCollection = Firebase.firestore.collection(USER_COLLECTION)
    var userDataListener: ListenerRegistration? = null
    var chatListener: ListenerRegistration? = null
    var chats by mutableStateOf<List<ChatData>>(emptyList())
    var tp by mutableStateOf(ChatData())
    var tpListener: ListenerRegistration? = null
    var reply by mutableStateOf("")
    private val firestore = FirebaseFirestore.getInstance()
    var msgListener: ListenerRegistration? = null
    var message by mutableStateOf<List<Message>>(listOf())
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

    fun getUserData(userId: String) {
        userDataListener = userCollection.document(userId).addSnapshotListener { value, error ->
            if (value != null) {
                val userData = value.toObject(UserData::class.java)
                _state.update {
                    it.copy(
                        userData = userData
                    )
                }
            }
        }
    }

    fun hideDialog() {
        _state.update {
            it.copy(
                showDialog = false
            )
        }
    }

    fun showDialog() {
        _state.update {
            it.copy(
                showDialog = true
            )
        }
    }

    fun setSrEmail(email: String) {
        _state.update {
            it.copy(
                srEmail = email
            )
        }
    }

    fun addChat(email: kotlin.String) {

        Firebase.firestore.collection(CHAT_COLLECTION).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.email", email),
                    Filter.equalTo("user2.email", state.value.userData?.email)
                ),
                Filter.and(
                    Filter.equalTo("user1.email", state.value.userData?.email),
                    Filter.equalTo("user2.email", email)
                )
            )
        ).get().addOnSuccessListener {
            if (it.isEmpty) {
                userCollection.whereEqualTo("email", email).get().addOnSuccessListener {
                    if (it.isEmpty) {
                        println("failed")
                    } else {

                        val chatPartner = it.toObjects(UserData::class.java).firstOrNull()
                        val id = Firebase.firestore.collection(CHAT_COLLECTION).document().id
                        val chat = ChatData(
                            chatId = id,
                            last = Message(
                                senderId = "",
                                content = "",
                                time = null
                            ),
                            user1 = ChatUserData(
                                userId = state.value.userData?.userId.toString(),
                                typing = false,
                                bio = "",
                                username = state.value.userData?.username.toString(),
                                ppurl = state.value.userData?.profilePictureUrl.toString(),
                                email = state.value.userData?.email.toString(),
                                status = false,
                            ),
                            user2 = ChatUserData(
                                userId = chatPartner?.userId.toString(),
                                email = chatPartner?.email.toString(),
                                typing = false,
                                bio = chatPartner?.bio.toString(),
                                username = chatPartner?.username.toString(),
                                ppurl = chatPartner?.profilePictureUrl.toString(),
                            )
                        )
                        Firebase.firestore.collection(CHAT_COLLECTION).document(id).set(chat)
                    }
                }
            }
        }
    }

    fun showChats(userId: String) {
        chatListener = Firebase.firestore.collection(CHAT_COLLECTION).where(
            Filter.or(
                Filter.equalTo("user1.userId", userId),
                Filter.equalTo("user2.userId", userId)
            )
        ).addSnapshotListener { value, error ->
            if (value != null) {
                chats = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }.sortedBy {
                    it.last?.time
                }.reversed()

            }

        }

    }

    fun getTp(chatId: String) {
        tpListener?.remove()
        tpListener = Firebase.firestore.collection(CHAT_COLLECTION).document(chatId)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    tp = value.toObject<ChatData>()!!
                }
            }
    }

    fun setChatUser(usr: ChatUserData, id: String) {
        _state.update {
            it.copy(
                User2 = usr,
                chatId = id
            )
        }
    }

    fun sendReply(
        chatId: String,
        replyMessage: Message = Message(),
        msg: String,
        senderId: String = state.value.userData?.userId.toString()

    ) {
        val id = Firebase.firestore.collection(CHAT_COLLECTION).document()
            .collection(MESSAGE_COLLECTION).document().id
        val time = Calendar.getInstance().time
        val message = Message(
            msgId = id,
            repliedMessage = replyMessage,
            senderId = senderId,
            content = msg,
            time = Timestamp(date = time)

        )
        Firebase.firestore.collection(CHAT_COLLECTION)
            .document(chatId)
            .collection(MESSAGE_COLLECTION)
            .document(id)
            .set(message)
        firestore.collection(CHAT_COLLECTION).document(chatId).update("last", message)
    }

    fun popMessage(chatId: String) {
        msgListener?.remove()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (chatId != null) {
                    msgListener = firestore.collection(CHAT_COLLECTION).document(chatId).collection(
                        MESSAGE_COLLECTION
                    ).addSnapshotListener { value, error ->
                        if (value != null) {
                            message = value.documents.mapNotNull {
                                it.toObject(Message::class.java)
                            }.sortedBy {
                                it.time
                            }.reversed()
                        }

                    }
                }
            }

        }
    }
}