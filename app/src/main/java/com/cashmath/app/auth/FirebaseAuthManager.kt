package com.cashmath.app.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.cashmath.app.R
import com.cashmath.app.utils.findActivity
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

object FirebaseAuthManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserSignedIn: Boolean
        get() = currentUser != null

    suspend fun signInWithGoogle(
        activity: FragmentActivity,
        webClientId: String? = null
    ): Result<FirebaseUser> {
        val serverClientId = webClientId ?: try {
            activity.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            "525493190847-2e50hrea557qpul7ho4ng1ogqk3kvjrs.apps.googleusercontent.com"
        }

        val credentialManager = CredentialManager.create(activity)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response = credentialManager.getCredential(
                request = request,
                context = activity
            )
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    Result.success(user)
                } else {
                    val err = Exception("Google sign-in returned null user")
                    Log.e("CashMathAuth", "Sign-in error: ${err.message}", err)
                    Toast.makeText(activity, "Sign-in error: ${err.message}", Toast.LENGTH_LONG).show()
                    Result.failure(err)
                }
            } else {
                val err = Exception("Invalid credential response type")
                Log.e("CashMathAuth", "Sign-in error: ${err.message}")
                Toast.makeText(activity, "Sign-in error: ${err.message}", Toast.LENGTH_LONG).show()
                Result.failure(err)
            }
        } catch (e: GetCredentialException) {
            Log.e("CashMathAuth", "CredentialManager failed: ${e.type} - ${e.message}", e)
            Toast.makeText(activity, "Sign-in error: ${e.message}", Toast.LENGTH_LONG).show()
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("CashMathAuth", "Sign-in exception: ${e.message}", e)
            Toast.makeText(activity, "Auth failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(Exception("Sign up failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sendOtp(
        context: Context,
        rawPhoneNumber: String,
        onCodeSent: (String) -> Unit,
        onVerificationFailed: (String) -> Unit,
        onAutoVerified: () -> Unit
    ) {
        val activity = context.findActivity()
        if (activity == null) {
            onVerificationFailed("Unable to find active screen window.")
            return
        }

        // Clean & ensure +91 prefix without spaces or dashes
        val digitsOnly = rawPhoneNumber.replace(Regex("[^0-9]"), "")
        val formattedPhone = when {
            rawPhoneNumber.startsWith("+") -> "+" + digitsOnly
            digitsOnly.length == 10 -> "+91$digitsOnly"
            digitsOnly.startsWith("91") && digitsOnly.length == 12 -> "+$digitsOnly"
            else -> "+91$digitsOnly"
        }

        if (formattedPhone.length < 12) {
            onVerificationFailed("Please enter a valid 10-digit mobile number.")
            return
        }

        try {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) onAutoVerified()
                                else onVerificationFailed(task.exception?.localizedMessage ?: "Verification failed")
                            }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        onVerificationFailed(e.localizedMessage ?: "SMS verification failed")
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        storedVerificationId = verificationId
                        resendToken = token
                        onCodeSent(verificationId)
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            onVerificationFailed(e.localizedMessage ?: "Unexpected error initializing OTP.")
        }
    }

    fun verifyOtp(
        otpCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val verificationId = storedVerificationId ?: run {
            onError("Verification session expired. Please request a new OTP.")
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode.trim())
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onError(task.exception?.localizedMessage ?: "Invalid OTP code entered")
            }
    }

    fun signOut(context: Context) {
        auth.signOut()
    }
}
