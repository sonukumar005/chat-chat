package com.example.chat_chat.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusTargetModifierNode
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chat_chat.ChatViewModel
import com.example.chat_chat.R
import com.example.chat_chat.googleSign.AppState
import com.example.chat_chat.googleSign.ChatUserData
import com.example.chat_chat.googleSign.Message


@OptIn(ExperimentalMaterial3Api::class, InternalComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatUI(
    viewModel: ChatViewModel = ChatViewModel(),
    userData: ChatUserData = ChatUserData(),
    chatId: String = "",
    message: List<Message> = emptyList(),
    state: AppState = AppState(),
    onBack: () -> Unit = {},
    context: Context = LocalContext.current,
    navController: NavController
) {
    val tp = viewModel.tp
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) {
        viewModel.popMessage(state.chatId)

    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = userData.ppurl, contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(40.dp),
                        )
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = userData.username,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            if (userData.userId == tp.user1?.userId) {
                                AnimatedVisibility(tp.user1.typing) {
                                    Text(
                                        text = "Typing..", modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                            if (userData.userId == tp.user2?.userId) {
                                AnimatedVisibility(tp.user2.typing) {
                                    Text(
                                        text = "Typing..", modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = null
                    )
                }
            )
        }
    ) {
        Image(
            painter = painterResource(id = R.drawable.blck_blurry),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(.5f)
        )
        Image(
            painter = painterResource(id = R.drawable.social_media_doodle_seamless_pattern_vector_27700734),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(.5f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .imePadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                Icon(imageVector = Icons.Rounded.CameraAlt, contentDescription = null)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(32.dp),
                            )
                ) {
                    TextField(
                        value = viewModel.reply, onValueChange = {
                            viewModel.reply = it
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(text = "Type a Message")
                        }, colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        )
                    )
                    AnimatedVisibility(visible = viewModel.reply.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .padding(12.dp)
                                .combinedClickable(
                                    onClick = {
                                        viewModel.sendReply(
                                            msg = viewModel.reply,
                                            chatId = chatId,
                                            //replyMessage = viewModel.replyMessage
                                        )
                                        viewModel.reply = ""
                                    },
                                    onDoubleClick = {

                                    }
                                )
                        )
                    }
                }

            }
        }


    }


}