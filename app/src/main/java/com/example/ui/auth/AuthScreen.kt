package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthScreen"

/**
 * Signs out from Firebase and clears Credential Manager session.
 */
fun signOut(
    context: Context? = null,
    credentialManager: CredentialManager? = null,
    onSignOutComplete: () -> Unit = {},
    scope: CoroutineScope? = null
) {
    try {
        Firebase.auth.signOut()
    } catch (_: Exception) {}

    if (credentialManager != null && context != null && scope != null) {
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {}
            onSignOutComplete()
        }
    } else {
        onSignOutComplete()
    }
}

/**
 * Clean Shop Logo Mark matching the reference image:
 * Elegant dual-petal minimalist white monogram on gradient.
 */
@Composable
fun MinimalistShopLogo(
    modifier: Modifier = Modifier,
    shopName: String = "MOFINOW"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dual-wing / petal stylized logo in pure white
        Canvas(modifier = Modifier.size(54.dp)) {
            val w = size.width
            val h = size.height

            // Left curved wing/leaf
            val leftWing = Path().apply {
                moveTo(w * 0.44f, h * 0.90f)
                cubicTo(
                    w * 0.38f, h * 0.70f,
                    w * 0.12f, h * 0.45f,
                    w * 0.16f, h * 0.16f
                )
                cubicTo(
                    w * 0.28f, h * 0.12f,
                    w * 0.46f, h * 0.32f,
                    w * 0.48f, h * 0.58f
                )
                close()
            }
            drawPath(path = leftWing, color = Color.White)

            // Right larger curved wing/leaf
            val rightWing = Path().apply {
                moveTo(w * 0.48f, h * 0.90f)
                cubicTo(
                    w * 0.54f, h * 0.65f,
                    w * 0.88f, h * 0.40f,
                    w * 0.82f, h * 0.12f
                )
                cubicTo(
                    w * 0.70f, h * 0.08f,
                    w * 0.52f, h * 0.30f,
                    w * 0.50f, h * 0.62f
                )
                close()
            }
            drawPath(path = rightWing, color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Brand / Shop Name
        Text(
            text = shopName,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.5.sp
        )
    }
}

/**
 * 4-Color Google Logo in white circle
 */
@Composable
fun GoogleBrandIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val stroke = w * 0.18f
        val radius = (w - stroke) / 2f

        // Top arc (Red)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 110f,
            useCenter = false,
            style = Stroke(width = stroke)
        )
        // Bottom-Left arc (Yellow)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 60f,
            useCenter = false,
            style = Stroke(width = stroke)
        )
        // Bottom arc (Green)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = stroke)
        )
        // Right bar and arc (Blue)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 45f,
            useCenter = false,
            style = Stroke(width = stroke)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(cx, cy),
            end = Offset(cx + radius + stroke / 2f, cy),
            strokeWidth = stroke
        )
    }
}

/**
 * Social media button with circular background matching reference image
 */
@Composable
fun SocialIconButton(
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF3868FA)
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        iconContent()
    }
}

/**
 * Login & Sign Up Screen accurately recreating the user's provided UI design:
 * - Fluid blue/purple gradient top with double wave curve
 * - Clean white minimalist shop logo ("Shop ka khali logo")
 * - Rounded pill input fields with light grey background
 * - Remember me & Forgot password
 * - Pill styled Login / Create Account button
 * - OR divider and social icons (Twitter, LinkedIn, Facebook, Google)
 * - Firebase Email/Password & Google Sign-In with Credential Manager
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialManager = remember { CredentialManager.create(context) }

    // Mode: "login", "signup", "welcome"
    var isSignUpMode by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    // Google Web Client ID
    val webClientId = remember(context) {
        try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            "171461343612-ov5keclpdhs1tr9i7k5htd94mkdeigid.apps.googleusercontent.com"
        }
    }

    // Google Sign-In with Credential Manager
    fun launchGoogleSignIn() {
        if (isGoogleLoading || isLoading) return
        coroutineScope.launch {
            isGoogleLoading = true
            try {
                val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken,
                        null
                    )
                    Firebase.auth.signInWithCredential(authCredential).await()
                    snackbarHostState.showSnackbar("लॉगिन सफल!")
                    onAuthSuccess()
                } else {
                    snackbarHostState.showSnackbar("Google क्रेडेंशियल प्राप्त नहीं हुआ")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "Google Sign-in cancelled by user")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                snackbarHostState.showSnackbar(e.localizedMessage ?: "Google लॉगिन विफल")
            } finally {
                isGoogleLoading = false
            }
        }
    }

    // Email/Password Login
    fun handleEmailLogin() {
        val userText = usernameOrEmail.trim()
        val pwd = password.trim()

        if (userText.isEmpty()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("कृपया Username या Email दर्ज करें") }
            return
        }
        if (pwd.length < 6) {
            coroutineScope.launch { snackbarHostState.showSnackbar("पासवर्ड कम से कम 6 अक्षरों का होना चाहिए") }
            return
        }

        val emailToUse = if (userText.contains("@")) userText else "$userText@steelcraft.app"

        coroutineScope.launch {
            isLoading = true
            focusManager.clearFocus()
            try {
                Firebase.auth.signInWithEmailAndPassword(emailToUse, pwd).await()
                snackbarHostState.showSnackbar("लॉगिन सफल!")
                onAuthSuccess()
            } catch (e: FirebaseAuthException) {
                val msg = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "यह खाता उपलब्ध नहीं है। कृपया Sign Up करें।"
                    "ERROR_WRONG_PASSWORD" -> "पासवर्ड गलत है।"
                    "ERROR_INVALID_EMAIL" -> "कृपया सही ईमेल दर्ज करें।"
                    else -> e.localizedMessage ?: "लॉगिन असफल रहा"
                }
                snackbarHostState.showSnackbar(msg)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.localizedMessage ?: "लॉगिन में त्रुटि आई")
            } finally {
                isLoading = false
            }
        }
    }

    // Email/Password Sign Up
    fun handleEmailSignUp() {
        val name = fullName.trim()
        val userText = usernameOrEmail.trim()
        val pwd = password.trim()
        val confirmPwd = confirmPassword.trim()

        if (name.isEmpty()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("कृपया अपना नाम दर्ज करें") }
            return
        }
        if (userText.isEmpty()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("कृपया Email या Username दर्ज करें") }
            return
        }
        if (pwd.length < 6) {
            coroutineScope.launch { snackbarHostState.showSnackbar("पासवर्ड कम से कम 6 अक्षरों का होना चाहिए") }
            return
        }
        if (pwd != confirmPwd) {
            coroutineScope.launch { snackbarHostState.showSnackbar("पासवर्ड और कन्फर्म पासवर्ड मेल नहीं खाते") }
            return
        }

        val emailToUse = if (userText.contains("@")) userText else "$userText@steelcraft.app"

        coroutineScope.launch {
            isLoading = true
            focusManager.clearFocus()
            try {
                val authResult = Firebase.auth.createUserWithEmailAndPassword(emailToUse, pwd).await()
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                authResult.user?.updateProfile(profileUpdates)?.await()

                snackbarHostState.showSnackbar("खाता सफलतापूर्वक बन गया!")
                onAuthSuccess()
            } catch (e: FirebaseAuthException) {
                val msg = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "यह ईमेल पहले से पंजीकृत है। कृपया Login करें।"
                    "ERROR_WEAK_PASSWORD" -> "पासवर्ड बहुत कमज़ोर है।"
                    else -> e.localizedMessage ?: "खाता बनाने में त्रुटि आई"
                }
                snackbarHostState.showSnackbar(msg)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.localizedMessage ?: "साइन अप में त्रुटि आई")
            } finally {
                isLoading = false
            }
        }
    }

    // Primary Colors from reference image
    val primaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2979FF), // Vibrant Blue
            Color(0xFF4A4BFA), // Indigo
            Color(0xFF6C3DF8)  // Violet / Purple
        )
    )
    val accentPurple = Color(0xFF7A42F4)
    val inputBackground = Color(0xFFF3F5F9)
    val brandBlue = Color(0xFF3868FA)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ================= TOP HEADER WITH DOUBLE WAVE =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                // Background Gradient with Wave Path
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Accent Purple Wave behind
                    val purpleWave = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.82f)
                        cubicTo(
                            w * 0.75f, h * 0.95f,
                            w * 0.55f, h * 0.88f,
                            w * 0.40f, h * 0.80f
                        )
                        cubicTo(
                            w * 0.25f, h * 0.72f,
                            w * 0.10f, h * 0.78f,
                            0f, h * 0.84f
                        )
                        close()
                    }
                    drawPath(
                        path = purpleWave,
                        color = accentPurple
                    )

                    // 2. Main Blue/Indigo Gradient with Organic Wave
                    val mainWave = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.75f)
                        cubicTo(
                            w * 0.80f, h * 0.86f,
                            w * 0.55f, h * 0.78f,
                            w * 0.35f, h * 0.65f
                        )
                        cubicTo(
                            w * 0.20f, h * 0.55f,
                            w * 0.08f, h * 0.65f,
                            0f, h * 0.72f
                        )
                        close()
                    }
                    drawPath(
                        path = mainWave,
                        brush = primaryGradient
                    )
                }

                // Shop Logo and Name Centered in Header
                MinimalistShopLogo(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 20.dp),
                    shopName = "MOFINOW"
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ================= SCREEN TITLE =================
            Text(
                text = if (isSignUpMode) "Create Account !" else "Welcome back !",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22262E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ================= INPUT FIELDS =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // If Sign Up: Full Name
                AnimatedVisibility(
                    visible = isSignUpMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = {
                                Text(
                                    "Full Name",
                                    color = Color(0xFFA0A5BA),
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(26.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBackground,
                                unfocusedContainerColor = inputBackground,
                                disabledContainerColor = inputBackground,
                                focusedBorderColor = brandBlue,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("input_fullname")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Username / Email Input
                OutlinedTextField(
                    value = usernameOrEmail,
                    onValueChange = { usernameOrEmail = it },
                    placeholder = {
                        Text(
                            if (isSignUpMode) "Email / Username" else "Username",
                            color = Color(0xFFA0A5BA),
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        if (usernameOrEmail.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Valid input",
                                tint = brandBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBackground,
                        unfocusedContainerColor = inputBackground,
                        disabledContainerColor = inputBackground,
                        focusedBorderColor = brandBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("input_username")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            "Password",
                            color = Color(0xFFA0A5BA),
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = Color(0xFFA0A5BA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBackground,
                        unfocusedContainerColor = inputBackground,
                        disabledContainerColor = inputBackground,
                        focusedBorderColor = brandBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isSignUpMode) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!isSignUpMode) handleEmailLogin()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("input_password")
                )

                // If Sign Up: Confirm Password
                AnimatedVisibility(
                    visible = isSignUpMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = {
                                Text(
                                    "Confirm Password",
                                    color = Color(0xFFA0A5BA),
                                    fontSize = 14.sp
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(26.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBackground,
                                unfocusedContainerColor = inputBackground,
                                disabledContainerColor = inputBackground,
                                focusedBorderColor = brandBlue,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { handleEmailSignUp() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("input_confirm_password")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Remember me & Forgot password row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Remember me
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { rememberMe = !rememberMe }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.5.dp,
                                    color = if (rememberMe) brandBlue else Color(0xFFA0A5BA),
                                    shape = CircleShape
                                )
                                .background(if (rememberMe) brandBlue else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rememberMe) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remember me",
                            fontSize = 12.sp,
                            color = Color(0xFF717E95)
                        )
                    }

                    // Forgot password?
                    Text(
                        text = "Forget password?",
                        fontSize = 12.sp,
                        color = Color(0xFF717E95),
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("पासवर्ड रीसेट लिंक आपके ईमेल पर भेजा जाएगा")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ================= PRIMARY ACTION BUTTON =================
                if (isSignUpMode) {
                    // Create Account: Gradient Pill Button
                    Button(
                        onClick = { handleEmailSignUp() },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brandBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_signup_submit")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Login: Outlined Pill Button matching the reference image!
                    OutlinedButton(
                        onClick = { handleEmailLogin() },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.5.dp, brandBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = brandBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_login_submit")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = brandBlue,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = brandBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ================= TOGGLE LOGIN / SIGN UP =================
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUpMode) "Already have an account? " else "New user? ",
                        fontSize = 13.sp,
                        color = Color(0xFF717E95)
                    )
                    Text(
                        text = if (isSignUpMode) "Login" else "Sign Up",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandBlue,
                        modifier = Modifier
                            .clickable {
                                isSignUpMode = !isSignUpMode
                            }
                            .testTag("btn_toggle_auth_mode")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ================= OR DIVIDER =================
                Row(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                    Text(
                        text = "OR",
                        fontSize = 12.sp,
                        color = Color(0xFFA0A5BA),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ================= SOCIAL LOGIN ROW =================
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Twitter / X
                    SocialIconButton(
                        backgroundColor = Color(0xFF1DA1F2),
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Twitter लॉगिन जल्द उपलब्ध होगा")
                            }
                        },
                        iconContent = {
                            Text(
                                "𝕏",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    )

                    // LinkedIn
                    SocialIconButton(
                        backgroundColor = Color(0xFF0077B5),
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("LinkedIn लॉगिन जल्द उपलब्ध होगा")
                            }
                        },
                        iconContent = {
                            Text(
                                "in",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    )

                    // Facebook
                    SocialIconButton(
                        backgroundColor = Color(0xFF3B5998),
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Facebook लॉगिन जल्द उपलब्ध होगा")
                            }
                        },
                        iconContent = {
                            Text(
                                "f",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                    )

                    // Google (Full Credential Manager + Firebase Auth Integration!)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { launchGoogleSignIn() }
                            .testTag("btn_google_signin"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = brandBlue
                            )
                        } else {
                            GoogleBrandIcon(modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in with another account",
                    fontSize = 11.sp,
                    color = Color(0xFFA0A5BA)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Guest Mode / Direct Explore
                TextButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier.testTag("btn_guest_mode")
                ) {
                    Text(
                        text = "बिना लॉगिन सीधे शुरू करें (Guest Mode)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF717E95)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Floating Snackbar for Error/Success notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
