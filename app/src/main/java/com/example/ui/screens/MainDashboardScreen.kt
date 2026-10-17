package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.AppScreen
import com.example.ui.AppViewModel
import com.example.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel,
    onNavigateToAuth: () -> Unit
) {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }

    val currentScreen by appViewModel.currentScreen.collectAsState()
    val notification by appViewModel.notification.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isGuest by authViewModel.isGuestMode.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(it.message)
            appViewModel.clearNotification()
        }
    }

    // Handle back button: return to Home tab if on another screen
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        appViewModel.navigateTo(AppScreen.HOME)
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
                        if (isGuest || currentUser == null) {
                            Text(
                                text = "Guest Mode (बिना लॉगिन)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            val userName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "User"
                            Text(
                                text = "Welcome, $userName",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    if (isGuest || currentUser == null) {
                        Button(
                            onClick = onNavigateToAuth,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("topbar_signin_button")
                        ) {
                            Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                authViewModel.signOut(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onComplete = {
                                        onNavigateToAuth()
                                    }
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
                    onClick = { appViewModel.navigateTo(AppScreen.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROJECTS,
                    onClick = { appViewModel.navigateTo(AppScreen.PROJECTS) },
                    icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Projects") },
                    label = { Text("Orders", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_projects")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.CALCULATOR,
                    onClick = { appViewModel.navigateTo(AppScreen.CALCULATOR) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                    label = { Text("Calc", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_calc")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.CATALOG,
                    onClick = { appViewModel.navigateTo(AppScreen.CATALOG) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Catalog") },
                    label = { Text("Gallery", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_catalog")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.AI_STUDIO,
                    onClick = { appViewModel.navigateTo(AppScreen.AI_STUDIO) },
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
                AppScreen.HOME -> HomeScreen(viewModel = appViewModel, onNavigateTo = { appViewModel.navigateTo(it) })
                AppScreen.PROJECTS -> ProjectsScreen(viewModel = appViewModel)
                AppScreen.CALCULATOR -> MeasurementCalculatorScreen(viewModel = appViewModel)
                AppScreen.CATALOG -> CatalogScreen(viewModel = appViewModel)
                AppScreen.AI_STUDIO -> AiStudioScreen(viewModel = appViewModel)
            }
        }
    }
}
