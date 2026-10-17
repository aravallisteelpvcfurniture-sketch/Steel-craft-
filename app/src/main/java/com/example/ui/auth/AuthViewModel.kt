package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val emailOrUsername: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SignUpUiState(
    val fullName: String = "",
    val emailOrUsername: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    // --- Login Form State Updaters ---
    fun onLoginEmailChange(value: String) {
        _loginState.value = _loginState.value.copy(emailOrUsername = value, errorMessage = null)
    }

    fun onLoginPasswordChange(value: String) {
        _loginState.value = _loginState.value.copy(password = value, errorMessage = null)
    }

    fun toggleLoginPasswordVisibility() {
        _loginState.value = _loginState.value.copy(isPasswordVisible = !_loginState.value.isPasswordVisible)
    }

    fun setLoginRememberMe(checked: Boolean) {
        _loginState.value = _loginState.value.copy(rememberMe = checked)
    }

    fun clearLoginError() {
        _loginState.value = _loginState.value.copy(errorMessage = null)
    }

    // --- Sign Up Form State Updaters ---
    fun onSignUpFullNameChange(value: String) {
        _signUpState.value = _signUpState.value.copy(fullName = value, errorMessage = null)
    }

    fun onSignUpEmailChange(value: String) {
        _signUpState.value = _signUpState.value.copy(emailOrUsername = value, errorMessage = null)
    }

    fun onSignUpPasswordChange(value: String) {
        _signUpState.value = _signUpState.value.copy(password = value, errorMessage = null)
    }

    fun onSignUpConfirmPasswordChange(value: String) {
        _signUpState.value = _signUpState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun toggleSignUpPasswordVisibility() {
        _signUpState.value = _signUpState.value.copy(isPasswordVisible = !_signUpState.value.isPasswordVisible)
    }

    fun clearSignUpError() {
        _signUpState.value = _signUpState.value.copy(errorMessage = null)
    }

    // --- Actions ---
    fun continueAsGuest(onSuccess: () -> Unit) {
        _isGuestMode.value = true
        onSuccess()
    }

    fun exitGuestMode() {
        _isGuestMode.value = false
    }

    fun login(onSuccess: () -> Unit) {
        val userText = _loginState.value.emailOrUsername.trim()
        val pwd = _loginState.value.password.trim()

        if (userText.isEmpty()) {
            _loginState.value = _loginState.value.copy(errorMessage = "कृपया Username या Email दर्ज करें")
            return
        }
        if (pwd.length < 6) {
            _loginState.value = _loginState.value.copy(errorMessage = "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए")
            return
        }

        val emailToUse = if (userText.contains("@")) userText else "$userText@steelcraft.app"

        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, errorMessage = null)
            try {
                auth.signInWithEmailAndPassword(emailToUse, pwd).await()
                _isGuestMode.value = false
                _loginState.value = _loginState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: FirebaseAuthException) {
                val msg = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "यह खाता उपलब्ध नहीं है। कृपया Sign Up करें।"
                    "ERROR_WRONG_PASSWORD" -> "पासवर्ड गलत है।"
                    "ERROR_INVALID_EMAIL" -> "कृपया सही ईमेल पता दर्ज करें।"
                    else -> e.localizedMessage ?: "लॉगिन असफल रहा"
                }
                _loginState.value = _loginState.value.copy(isLoading = false, errorMessage = msg)
            } catch (e: Exception) {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "लॉगिन में त्रुटि आई"
                )
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        val name = _signUpState.value.fullName.trim()
        val userText = _signUpState.value.emailOrUsername.trim()
        val pwd = _signUpState.value.password.trim()
        val confirmPwd = _signUpState.value.confirmPassword.trim()

        if (name.isEmpty()) {
            _signUpState.value = _signUpState.value.copy(errorMessage = "कृपया अपना पूरा नाम दर्ज करें")
            return
        }
        if (userText.isEmpty()) {
            _signUpState.value = _signUpState.value.copy(errorMessage = "कृपया Email या Username दर्ज करें")
            return
        }
        if (pwd.length < 6) {
            _signUpState.value = _signUpState.value.copy(errorMessage = "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए")
            return
        }
        if (pwd != confirmPwd) {
            _signUpState.value = _signUpState.value.copy(errorMessage = "पासवर्ड और कन्फर्म पासवर्ड मेल नहीं खाते")
            return
        }

        val emailToUse = if (userText.contains("@")) userText else "$userText@steelcraft.app"

        viewModelScope.launch {
            _signUpState.value = _signUpState.value.copy(isLoading = true, errorMessage = null)
            try {
                val authResult = auth.createUserWithEmailAndPassword(emailToUse, pwd).await()
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                authResult.user?.updateProfile(profileUpdates)?.await()

                _isGuestMode.value = false
                _signUpState.value = _signUpState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: FirebaseAuthException) {
                val msg = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "यह ईमेल पहले से पंजीकृत है। कृपया Login करें।"
                    "ERROR_WEAK_PASSWORD" -> "पासवर्ड बहुत कमज़ोर है।"
                    else -> e.localizedMessage ?: "खाता बनाने में त्रुटि आई"
                }
                _signUpState.value = _signUpState.value.copy(isLoading = false, errorMessage = msg)
            } catch (e: Exception) {
                _signUpState.value = _signUpState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "साइन अप में त्रुटि आई"
                )
            }
        }
    }

    fun signInWithGoogleCredential(
        credential: AuthCredential,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isGoogleLoading = true)
            _signUpState.value = _signUpState.value.copy(isGoogleLoading = true)
            try {
                auth.signInWithCredential(credential).await()
                _isGuestMode.value = false
                _loginState.value = _loginState.value.copy(isGoogleLoading = false)
                _signUpState.value = _signUpState.value.copy(isGoogleLoading = false)
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Google साइन इन विफल रहा"
                _loginState.value = _loginState.value.copy(isGoogleLoading = false, errorMessage = errorMsg)
                _signUpState.value = _signUpState.value.copy(isGoogleLoading = false, errorMessage = errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.signOut()
            } catch (_: Exception) {}
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {}
            _isGuestMode.value = false
            onComplete()
        }
    }
}
