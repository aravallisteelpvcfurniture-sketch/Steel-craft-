package com.example.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.auth.AuthViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

private const val TAG = "SignUpScreen"

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialManager = remember { CredentialManager.create(context) }

    val state by authViewModel.signUpState.collectAsState()

    BackHandler {
        onNavigateToLogin()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            authViewModel.clearSignUpError()
        }
    }

    val webClientId = remember(context) {
        try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            "171461343612-ov5keclpdhs1tr9i7k5htd94mkdeigid.apps.googleusercontent.com"
        }
    }

    fun launchGoogleSignIn() {
        if (state.isGoogleLoading || state.isLoading) return
        coroutineScope.launch {
            try {
                val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId).build()
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
                    authViewModel.signInWithGoogleCredential(
                        credential = authCredential,
                        onSuccess = {
                            coroutineScope.launch { snackbarHostState.showSnackbar("लॉगिन सफल!") }
                            onSignUpSuccess()
                        },
                        onError = { err ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(err) }
                        }
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "Google Sign-in cancelled by user")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                snackbarHostState.showSnackbar(e.localizedMessage ?: "Google साइन इन विफल")
            }
        }
    }

    val primaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2979FF),
            Color(0xFF4A4BFA),
            Color(0xFF6C3DF8)
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
            // Wave Header with Back Button and Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val purpleWave = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.82f)
                        cubicTo(w * 0.75f, h * 0.95f, w * 0.55f, h * 0.88f, w * 0.40f, h * 0.80f)
                        cubicTo(w * 0.25f, h * 0.72f, w * 0.10f, h * 0.78f, 0f, h * 0.84f)
                        close()
                    }
                    drawPath(path = purpleWave, color = accentPurple)

                    val mainWave = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.75f)
                        cubicTo(w * 0.80f, h * 0.86f, w * 0.55f, h * 0.78f, w * 0.35f, h * 0.65f)
                        cubicTo(w * 0.20f, h * 0.55f, w * 0.08f, h * 0.65f, 0f, h * 0.72f)
                        close()
                    }
                    drawPath(path = mainWave, brush = primaryGradient)
                }

                // Back Button
                IconButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .padding(top = 36.dp, start = 16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = Color.White
                    )
                }

                // Minimalist Shop Logo
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Canvas(modifier = Modifier.size(46.dp)) {
                        val w = size.width
                        val h = size.height
                        val leftWing = Path().apply {
                            moveTo(w * 0.44f, h * 0.90f)
                            cubicTo(w * 0.38f, h * 0.70f, w * 0.12f, h * 0.45f, w * 0.16f, h * 0.16f)
                            cubicTo(w * 0.28f, h * 0.12f, w * 0.46f, h * 0.32f, w * 0.48f, h * 0.58f)
                            close()
                        }
                        drawPath(path = leftWing, color = Color.White)

                        val rightWing = Path().apply {
                            moveTo(w * 0.48f, h * 0.90f)
                            cubicTo(w * 0.54f, h * 0.65f, w * 0.88f, h * 0.40f, w * 0.82f, h * 0.12f)
                            cubicTo(w * 0.70f, h * 0.08f, w * 0.52f, h * 0.30f, w * 0.50f, h * 0.62f)
                            close()
                        }
                        drawPath(path = rightWing, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "STEELCRAFT",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Create Account !",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22262E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Full Name
                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = { authViewModel.onSignUpFullNameChange(it) },
                    placeholder = {
                        Text("Full Name", color = Color(0xFFA0A5BA), fontSize = 14.sp)
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
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("input_fullname")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Username / Email
                OutlinedTextField(
                    value = state.emailOrUsername,
                    onValueChange = { authViewModel.onSignUpEmailChange(it) },
                    placeholder = {
                        Text("Email / Username", color = Color(0xFFA0A5BA), fontSize = 14.sp)
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
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("input_signup_username")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { authViewModel.onSignUpPasswordChange(it) },
                    placeholder = {
                        Text("Password (min 6 chars)", color = Color(0xFFA0A5BA), fontSize = 14.sp)
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
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { authViewModel.toggleSignUpPasswordVisibility() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
                                tint = Color(0xFFA0A5BA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("input_signup_password")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Password
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { authViewModel.onSignUpConfirmPasswordChange(it) },
                    placeholder = {
                        Text("Confirm Password", color = Color(0xFFA0A5BA), fontSize = 14.sp)
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
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            authViewModel.signUp(onSignUpSuccess)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("input_signup_confirm_password")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sign Up Primary Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        authViewModel.signUp(onSignUpSuccess)
                    },
                    enabled = !state.isLoading && !state.isGoogleLoading,
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = brandBlue)
                        .testTag("signup_button")
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE5E7EB)))
                    Text(
                        text = "Or continue with",
                        fontSize = 12.sp,
                        color = Color(0xFFA0A5BA),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE5E7EB)))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Google Sign In Button
                Button(
                    onClick = { launchGoogleSignIn() },
                    enabled = !state.isGoogleLoading && !state.isLoading,
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3F5F9),
                        contentColor = Color(0xFF22262E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_signup_button")
                ) {
                    if (state.isGoogleLoading) {
                        CircularProgressIndicator(
                            color = brandBlue,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G ", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text("Google Sign-In", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Switch to Login
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontSize = 13.sp,
                        color = Color(0xFF7A7E89)
                    )
                    Text(
                        text = "Log In",
                        fontSize = 13.sp,
                        color = brandBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onNavigateToLogin() }
                            .testTag("navigate_login_button")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Guest Mode option
                TextButton(
                    onClick = {
                        authViewModel.continueAsGuest(onContinueAsGuest)
                    },
                    modifier = Modifier.testTag("signup_guest_mode_button")
                ) {
                    Text(
                        text = "Continue as Guest / बिना लॉगिन देखें",
                        fontSize = 12.sp,
                        color = Color(0xFF5A6072),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
