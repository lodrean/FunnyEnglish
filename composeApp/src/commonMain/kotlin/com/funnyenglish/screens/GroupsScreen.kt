package com.funnyenglish.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.viewmodel.GroupsViewModel
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.shared.model.GroupDetail
import com.funnyenglish.shared.model.GroupMember
import com.funnyenglish.shared.model.JoinGroupResponse
import com.funnyenglish.shared.model.StudentGroup
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Экран "Мои группы" для учеников
 * 
 * Позволяет:
 * - Просматривать список групп, в которых состоит ученик
 * - Видеть участников группы
 * - Присоединяться к новым группам по коду
 * - Покидать группы
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    isGuest: Boolean = false,
    onNavigate: (com.funnyenglish.app.AppScreen) -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    if (isGuest) {
        GuestGroupsStub(
            onNavigateBack = onNavigateBack,
            onLoginClick = { onNavigate(com.funnyenglish.app.AppScreen.Login) }
        )
        return
    }

    val viewModel: GroupsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    // Show error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Show success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои группы") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showJoinDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Присоединиться к группе")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.groups.isEmpty() -> {
                    EmptyGroupsView(
                        onJoinClick = { viewModel.showJoinDialog() }
                    )
                }
                else -> {
                    GroupsList(
                        groups = uiState.groups,
                        onGroupClick = { group ->
                            onNavigateToGroupDetail(group.id)
                        }
                    )
                }
            }

            // Join Group Dialog
            if (uiState.showJoinDialog) {
                JoinGroupDialog(
                    inviteCode = uiState.inviteCodeInput,
                    isLoading = uiState.isJoining,
                    onCodeChange = viewModel::onInviteCodeChange,
                    onDismiss = viewModel::hideJoinDialog,
                    onJoin = { viewModel.joinGroup() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuestGroupsStub(
    onNavigateBack: () -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои группы") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(SpaceXl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(SpaceLg))

            Text(
                text = "Группы доступны только авторизованным пользователям",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(SpaceSm))

            Text(
                text = "Войдите или зарегистрируйтесь, чтобы присоединиться к группам",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(SpaceXl))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Войти или зарегистрироваться")
            }
        }
    }
}

@Composable
private fun EmptyGroupsView(
    onJoinClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Groups,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(SpaceLg))
        
        Text(
            text = "Вы не состоите ни в одной группе",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(SpaceSm))
        
        Text(
            text = "Присоединитесь к группе вашего преподавателя, чтобы видеть прогресс одноклассников",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(SpaceXl))
        
        Button(
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(SpaceSm))
            Text("Присоединиться к группе")
        }
    }
}

@Composable
private fun GroupsList(
    groups: List<StudentGroup>,
    onGroupClick: (StudentGroup) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SpaceMd),
        verticalArrangement = Arrangement.spacedBy(SpaceMd)
    ) {
        items(groups, key = { it.id }) { group ->
            GroupCard(
                group = group,
                onClick = { onGroupClick(group) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupCard(
    group: StudentGroup,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    group.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
                
                if (!group.isActive) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Неактивна") },
                        enabled = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(SpaceSm))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpaceSm)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${group.currentStudents} / ${group.maxStudents} учеников",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text("•")
                
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Преподаватель: ${group.teacherName ?: "Неизвестно"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun JoinGroupDialog(
    inviteCode: String,
    isLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Присоединиться к группе")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть")
                }
            }
        },
        text = {
            Column {
                Text(
                    "Введите код приглашения от вашего преподавателя",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(SpaceMd))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = onCodeChange,
                    label = { Text("Код приглашения") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    supportingText = { Text("Код состоит из 6-8 символов") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onJoin,
                enabled = inviteCode.length >= 4 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Присоединиться")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

// ==================== Group Detail Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: GroupsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        viewModel.loadGroupDetail(groupId)
    }

    // Show messages
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { 
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    val groupDetail = uiState.selectedGroup

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupDetail?.name ?: "Группа") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    groupDetail?.let { group ->
                        var showMenu by remember { mutableStateOf(false) }
                        
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Покинуть группу") },
                                leadingIcon = { 
                                    Icon(Icons.Default.ExitToApp, contentDescription = null) 
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.leaveGroup(group.id)
                                    onNavigateBack()
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                groupDetail == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Группа не найдена")
                        Button(onClick = onNavigateBack) {
                            Text("Назад")
                        }
                    }
                }
                else -> {
                    GroupDetailContent(
                        group = groupDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupDetailContent(
    group: GroupDetail
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SpaceMd),
        verticalArrangement = Arrangement.spacedBy(SpaceMd)
    ) {
        // Group Info Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpaceMd)
                ) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    group.description?.let {
                        if (it.isNotBlank()) {
                            Spacer(modifier = Modifier.height(SpaceSm))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(SpaceMd))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SpaceMd)
                    ) {
                        GroupInfoChip(
                            icon = Icons.Default.School,
                            text = group.teacherName ?: "Преподаватель"
                        )
                        GroupInfoChip(
                            icon = Icons.Default.Person,
                            text = "${group.members.size} / ${group.maxStudents}"
                        )
                    }
                }
            }
        }

        // Members Section
        item {
            Text(
                text = "Участники группы",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = SpaceSm)
            )
        }

        items(group.members, key = { it.id }) { member ->
            MemberCard(member = member)
        }
    }
}

@Composable
private fun GroupInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpaceXs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MemberCard(
    member: GroupMember
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            // Avatar placeholder
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = member.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpaceSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ур. ${member.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${member.totalPoints} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Streak
            if (member.currentStreak > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpaceXs)
                ) {
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = member.currentStreak.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
