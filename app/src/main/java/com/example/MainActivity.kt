package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.AppViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeasurementCalculatorScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isGuestMode by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null && !isGuestMode) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onContinueAsGuest = { isGuestMode = true }
        )
    } else {
        MainScreen(
            isGuest = currentUser == null,
            onSignInClick = { isGuestMode = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AppViewModel = viewModel(),
    isGuest: Boolean = false,
    onSignInClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val currentScreen by viewModel.currentScreen.collectAsState()
    val notification by viewModel.notification.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.clearNotification()
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.navigateTo(AppScreen.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (currentScreen) {
                                AppScreen.HOME -> "SteelCraft Pro"
                                AppScreen.PROJECTS -> "Work Orders"
                                AppScreen.CALCULATOR -> "Measurement Calc"
                                AppScreen.CATALOG -> "Photo Gallery"
                                AppScreen.AI_STUDIO -> "AI Design Studio"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        if (isGuest) {
                            Text(
                                text = "Guest / Offline Mode (बिना लॉगिन)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    if (isGuest) {
                        androidx.compose.material3.Button(
                            onClick = onSignInClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                signOut(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = { /* State listener handles null currentUser */ },
                                    scope = coroutineScope
                                )
                            },
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.HOME,
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROJECTS,
                    onClick = { viewModel.navigateTo(AppScreen.PROJECTS) },
                    icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Projects") },
                    label = { Text("Orders", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_projects")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.CALCULATOR,
                    onClick = { viewModel.navigateTo(AppScreen.CALCULATOR) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                    label = { Text("Calc", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_calc")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.CATALOG,
                    onClick = { viewModel.navigateTo(AppScreen.CATALOG) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Catalog") },
                    label = { Text("Gallery", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_catalog")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.AI_STUDIO,
                    onClick = { viewModel.navigateTo(AppScreen.AI_STUDIO) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Studio") },
                    label = { Text("AI Studio", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_ai")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(viewModel = viewModel, onNavigateTo = { viewModel.navigateTo(it) })
                AppScreen.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                AppScreen.CALCULATOR -> MeasurementCalculatorScreen(viewModel = viewModel)
                AppScreen.CATALOG -> CatalogScreen(viewModel = viewModel)
                AppScreen.AI_STUDIO -> AiStudioScreen(viewModel = viewModel)
            }
        }
    }
}
