package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.HadiyaRecord
import com.example.ui.components.AddHadiyaSheet
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EditHadiyaDialog
import com.example.ui.components.PinUnlockDialog
import com.example.ui.components.RecordDetailDialog
import com.example.ui.components.WhatsAppSuccessDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.HadiyaBookTheme
import com.example.ui.viewmodel.HadiyaViewModel
import com.example.ui.viewmodel.ThemeMode
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    data object Reports : Screen("reports", "Reports", Icons.Filled.Assessment, Icons.Outlined.Assessment)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: HadiyaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            HadiyaBookTheme(darkTheme = useDarkTheme) {
                HadiyaBookApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadiyaBookApp(viewModel: HadiyaViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val isLocked by viewModel.isLocked.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog & Sheet States
    var showAddSheet by remember { mutableStateOf(false) }
    var savedRecordForWhatsApp by remember { mutableStateOf<HadiyaRecord?>(null) }
    var selectedRecordForDetail by remember { mutableStateOf<HadiyaRecord?>(null) }
    var recordToEdit by remember { mutableStateOf<HadiyaRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<HadiyaRecord?>(null) }

    // Listen to ViewModel snackbar events
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val screens = listOf(Screen.Home, Screen.History, Screen.Reports, Screen.Settings)

    // Modals
    if (isLocked) {
        PinUnlockDialog(
            onUnlock = { pin -> viewModel.unlockApp(pin) }
        )
    }

    if (showAddSheet) {
        AddHadiyaSheet(
            viewModel = viewModel,
            onDismiss = { showAddSheet = false },
            onRecordSaved = { record ->
                savedRecordForWhatsApp = record
            }
        )
    }

    savedRecordForWhatsApp?.let { record ->
        WhatsAppSuccessDialog(
            record = record,
            onDismiss = { savedRecordForWhatsApp = null }
        )
    }

    selectedRecordForDetail?.let { record ->
        RecordDetailDialog(
            record = record,
            onDismiss = { selectedRecordForDetail = null },
            onEdit = {
                selectedRecordForDetail = null
                recordToEdit = record
            },
            onDelete = {
                selectedRecordForDetail = null
                recordToDelete = record
            }
        )
    }

    recordToEdit?.let { record ->
        EditHadiyaDialog(
            record = record,
            onDismiss = { recordToEdit = null },
            onSave = { name, phone, amount, date, note ->
                viewModel.updateRecord(record, name, phone, amount, date, note) {
                    recordToEdit = null
                }
            }
        )
    }

    recordToDelete?.let { record ->
        DeleteConfirmDialog(
            record = record,
            onConfirm = {
                viewModel.deleteRecord(record) {
                    recordToDelete = null
                }
            },
            onDismiss = { recordToDelete = null }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            Screen.Home.route -> "HadiyaBook"
                            Screen.History.route -> "History"
                            Screen.Reports.route -> "Reports"
                            Screen.Settings.route -> "Settings"
                            else -> "HadiyaBook"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = "HadiyaBook",
                            tint = EmeraldGreen
                        )
                    }
                },
                actions = {
                    if (currentRoute == Screen.Home.route || currentRoute == Screen.History.route) {
                        IconButton(
                            onClick = { showAddSheet = true },
                            modifier = Modifier.testTag("topbar_add_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Hadiya",
                                tint = EmeraldGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                screens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldGreen,
                            selectedTextColor = EmeraldGreen,
                            indicatorColor = EmeraldGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentRoute == Screen.Home.route || currentRoute == Screen.History.route
            ) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = EmeraldGreen,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_hadiya")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Hadiya", modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onOpenAddHadiya = { showAddSheet = true },
                        onNavigateToHistory = {
                            navController.navigate(Screen.History.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToReports = {
                            navController.navigate(Screen.Reports.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onRecordClick = { record ->
                            selectedRecordForDetail = record
                        }
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = viewModel,
                        onOpenAddHadiya = { showAddSheet = true },
                        onViewRecord = { record ->
                            selectedRecordForDetail = record
                        },
                        onEditRecord = { record ->
                            recordToEdit = record
                        },
                        onDeleteRecord = { record ->
                            recordToDelete = record
                        }
                    )
                }

                composable(Screen.Reports.route) {
                    ReportsScreen(
                        viewModel = viewModel,
                        onOpenAddHadiya = { showAddSheet = true },
                        onViewRecord = { record ->
                            selectedRecordForDetail = record
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
