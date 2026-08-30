package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.viewmodel.MessagesState
import com.sotospeak.shared.contracts.Message
import com.sotospeak.shared.contracts.MessageType

/**
 * Экран входящих сообщений от учителя (inbox).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    state: MessagesState,
    onLoad: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoad()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сообщения") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .testTag("messages_screen")
        ) {
            when {
                state.isLoading -> LoadingIndicator()
                state.error != null -> ErrorMessage(
                    message = state.error,
                    onRetry = onLoad
                )
                state.messages.isEmpty() -> EmptyMessagesPlaceholder()
                else -> MessagesList(
                    messages = state.messages,
                    onMarkAsRead = onMarkAsRead
                )
            }
        }
    }
}

@Composable
private fun EmptyMessagesPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📭", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Пока нет сообщений",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Здесь появятся сообщения и комментарии от учителя",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MessagesList(
    messages: List<Message>,
    onMarkAsRead: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageCard(
                message = message,
                onMarkAsRead = { onMarkAsRead(message.id) }
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: Message,
    onMarkAsRead: () -> Unit
) {
    val isUnread = message.readAt == null

    LaunchedEffect(message.id) {
        if (isUnread) onMarkAsRead()
    }

    // M3 (A14): Card + ListItem (avatar + 2 строки), непрочитанное — Badge
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_card_${message.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            supportingContent = {
                Column {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMessageDate(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingContent = {
                BadgedBox(badge = { if (isUnread) Badge() }) {
                    MessageAvatar(senderName = message.senderName)
                }
            },
            trailingContent = {
                val isComment = message.type == MessageType.COMMENT
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (isComment) "Комментарий" else "Сообщение",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isComment) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        },
                        labelColor = if (isComment) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    border = null
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
    }
}

/** Аватар отправителя: круг primaryContainer с инициалами. */
@Composable
private fun MessageAvatar(senderName: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = messageInitials(senderName),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/** Инициалы аватара: первые буквы первых двух слов. */
private fun messageInitials(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

/** createdAt приходит в ISO-формате; показываем дату и время коротко */
private fun formatMessageDate(iso: String): String {
    // "2026-07-21T07:27:23.882389Z" → "21.07.2026 07:27"
    return try {
        val date = iso.substringBefore("T")
        val time = iso.substringAfter("T").take(5)
        val (y, m, d) = date.split("-")
        "$d.$m.$y $time"
    } catch (_: Exception) {
        iso
    }
}
