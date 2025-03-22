package com.example.chat_chat.screen

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.media.tv.interactive.AppLinkInfo
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.chat_chat.ChatViewModel
import com.example.chat_chat.Dialogs.CustomDialogBox
import com.example.chat_chat.R
import com.example.chat_chat.googleSign.AppState
import com.example.chat_chat.googleSign.ChatData
import com.example.chat_chat.googleSign.ChatUserData
import com.google.android.gms.maps.model.Circle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.setValue



@Composable
fun ChatsScreenUI(
    viewModel: ChatViewModel,
    state: AppState = AppState(),
    showSingleChat: (ChatUserData, String) -> Unit
) {
    val padding by animateDpAsState(
        targetValue = 10.dp,
        label = ""
    )
    val chats = viewModel.chats
    val filterChats = chats
    val selectedItem = remember {
        mutableStateListOf<String>()
    }
    val border = Brush.sweepGradient(
        listOf(
            Color(0xFFA7e6FF),
            Color(0xFFA7E6FF)
        )
    )
    var bitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var imgUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult( contract = ActivityResultContracts.GetContent() ) {
       imgUri = it
    }
    var isUploading by remember {
        mutableStateOf(false)
    }
    BackHandler {
       selectedItem.clear()
        imgUri = null
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showDialog() },
                shape = RoundedCornerShape(50.dp),
                containerColor = colorScheme.inversePrimary
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    ) {
        it
        Image(
            painter = painterResource(id = R.drawable.blck_blurry),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = Crop
        )
        AnimatedVisibility(visible = state.showDialog) {
            CustomDialogBox(
                state = state,
                hideDialog = { viewModel.hideDialog() },
                addChat = {
                    viewModel.addChat(state.srEmail)
                    viewModel.hideDialog()
                    viewModel.setSrEmail("")
                },
                setEmail = {
                    viewModel.setSrEmail(it)
                }
            )
        }
        imgUri?.let{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                var src = ImageDecoder.createSource(LocalContext.current.contentResolver,it)
                bitmap = ImageDecoder.decodeBitmap(src)

            }
          
        }
        Column(
            modifier = Modifier.padding(top = 36.dp)
        ) {
            Box() {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(0.98f)
                ) {
                    Column {
                        Text(
                            text = "Hello, ",
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .offset(y = 5.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = state.userData?.username.toString(),
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 20.sp)
                        )

                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .background(
                                colorScheme.background.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .border(
                                0.05.dp,
                                color = Color(0xFF35567A),
                                CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable._666693_search_icon),
                            contentDescription = null,
                            modifier = Modifier.scale(0.7f)
                        )

                    }
                    Column() {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .background(
                                    colorScheme.background.copy(alpha = .2f),
                                    CircleShape
                                )
                                .border(0.05.dp, Color(0xFF35567A), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.scale(1.3f)
                            )
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(top = padding)
                    .fillMaxSize()
                    .background(
                        color = colorScheme.background.copy(alpha = .2f),
                        shape = RoundedCornerShape(30.dp, 30.dp)
                    )
                    .border(
                        0.05.dp,
                        color = Color(0xFF35567A),
                        shape = RoundedCornerShape(30.dp, 30.dp),


                        )
            ) {
                item {
                    Text(
                        text = "Chats",
                        modifier = Modifier
                            .padding(start = padding)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                }
                items(filterChats) {
                    val chatUser = if (
                        it.user1?.userId != state.userData?.userId
                    ) {
                        it.user1
                    } else {
                        it.user2

                    }
                    ChatItem(
                        state = state,
                        chatUser!!,
                        chat = it,
                        isSelected = selectedItem.contains(it.chatId),
                        showSingleChat = { user, id -> showSingleChat(user, id) }

                    )
                }
            }
        }


    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    state: AppState,
    userData: ChatUserData,
    chat: ChatData,
    isSelected: Boolean,
    showSingleChat: (ChatUserData, String) -> Unit
) {
    val formatter: SimpleDateFormat = remember {
        SimpleDateFormat(("hh:mm a"), java.util.Locale.getDefault())
    }
    val color = if (!isSelected) Color.Transparent else colorScheme.onPrimary
    Row(
        modifier = Modifier
            .background(color = color)
            .fillMaxWidth()
            .clickable {
                showSingleChat(
                    userData, chat.chatId
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(userData.ppurl)
                .crossfade(true)
                .allowHardware(false)
                .build(),
            placeholder = painterResource(id = R.drawable.person_placeholder_4),
            error = painterResource(id = R.drawable.person_placeholder_4),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(.95f)

            ) {
                Text(
                    text = if (userData.userId == state.userData?.userId) userData.username.orEmpty() + "You"
                    else userData.username,
                    modifier = Modifier.width(150.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (chat.last?.time != null) formatter.format(chat.last.time.toDate()) else "",
                    color = Color.Gray,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Light
                    ),

                    )

            }
            AnimatedVisibility(chat.last?.content != null && userData.typing) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()

                ) {
                    if (chat.last?.senderId == state.userData?.userId) {
                        Icon(
                            painter = painterResource(id = R.drawable.check_mark),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(10.dp),
                            tint = if (chat.last?.read ?: false) Color(0xFF13C70D) else Color.White
                        )


                    }
                }
            }

        }
    }

}

