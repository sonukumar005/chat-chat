package com.example.chat_chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chat_chat.googleSign.GoogleAuthUiClient
import com.example.chat_chat.screen.ChatUI

import com.example.chat_chat.screen.ChatsScreenUI

import com.example.chat_chat.screen.SignInScreenUI
import com.example.chat_chat.ui.theme.CHATCHATTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            viewModel = viewModel,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CHATCHATTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val state = viewModel.state.collectAsState()
                        val navController = rememberNavController()

                        NavHost(navController = navController, startDestination = startScreen) {

                            composable<startScreen> {
                                LaunchedEffect(key1 = Unit) {
                                    val userData = googleAuthUiClient.getSignedInUser()
                                    if (userData != null) {
                                        viewModel.getUserData(userData.userId)
                                        viewModel.showChats(userData.userId)
                                        navController.navigate(chatsScreen)
                                    } else {
                                        navController.navigate(SignInScreen)
                                    }
                                }
                            }
                            composable<SignInScreen> {
                                val launcher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                                    onResult = { result ->
                                        if (result.resultCode == RESULT_OK) {
                                            lifecycleScope.launch {
                                                val signInResult =
                                                    googleAuthUiClient.signInWithIntent(
                                                        intent = result.data ?: return@launch

                                                    )
                                                viewModel.onSignInResult(signInResult)
                                            }
                                        }
                                    }
                                )
                                LaunchedEffect(key1 = state.value.isSignedIn) {
                                    val userData = googleAuthUiClient.getSignedInUser()
                                    userData?.run{
                                        viewModel.addUserToFirestore(userData)
                                        viewModel.getUserData(userData.userId)
                                         viewModel.showChats(userData.userId)
                                    }
                                    if (state.value.isSignedIn) {
                                        navController.navigate(chatsScreen)
                                    }

                                }
                                SignInScreenUI(onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                                )
                            }
                            composable<chatsScreen> {

                                ChatsScreenUI(viewModel,
                                    state.value,
                                    showSingleChat = {
                                                     usr, id ->
                                        viewModel.getTp(id)
                                        viewModel.setChatUser(usr,id)
                                        navController.navigate(ChatScreen)
                                    }
                                    )
                            }
                            composable<ChatScreen>(enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth->
                                        fullWidth
                                    },
                                    animationSpec = tween(durationMillis = 300)
                                )
                            },
                                exitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth->
                                            -fullWidth
                                        },
                                        animationSpec = tween(durationMillis = 300)
                                    )
                                }) {

                                ChatUI(
                                    viewModel = viewModel,
                                    navController = navController,
                                    userData = state.value.User2!!,
                                    chatId = state.value.chatId,
                                    state = state.value, onBack = {},

                                )

                            }
                        }
                    }
                }
            }
        }
    }
}




