package com.cashmath.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseUser
import com.cashmath.app.auth.FirebaseAuthManager
import com.cashmath.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: (FirebaseUser?) -> Unit,
    onContinueOffline: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()

    var selectedAuthTab by remember { mutableIntStateOf(0) } // 0 = Phone OTP, 1 = Email
    var isSignUpMode by remember { mutableStateOf(false) }

    var phoneText by remember { mutableStateOf("") }
    var otpText by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = RoyalIndigo,
        focusedBorderColor = RoyalIndigo,
        unfocusedBorderColor = BorderHairline
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BrandIndigoLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = RoyalIndigo,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to CashMath",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Sign in to CashMath to enable Cloud Backup & Sync across devices",
                    fontSize = 12.sp,
                    color = SlateGray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Google Sign-In Button
                Button(
                    onClick = {
                        if (activity != null) {
                            isLoading = true
                            scope.launch {
                                val res = FirebaseAuthManager.signInWithGoogle(activity)
                                isLoading = false
                                res.onSuccess { user ->
                                    Toast.makeText(context, "Welcome ${user.displayName ?: user.email}!", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess(user)
                                }.onFailure { err ->
                                    Toast.makeText(context, "Google Sign-In: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2646)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Outline)
                    Text("  OR  ", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Outline)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sub-Tab Switcher (Phone OTP vs Email)
                TabRow(
                    selectedTabIndex = selectedAuthTab,
                    containerColor = Surface,
                    contentColor = RoyalIndigo
                ) {
                    Tab(
                        selected = selectedAuthTab == 0,
                        onClick = { selectedAuthTab = 0 },
                        text = { Text("Phone OTP", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedAuthTab == 1,
                        onClick = { selectedAuthTab = 1 },
                        text = { Text("Email Login", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedAuthTab == 0) {
                    // PHONE OTP LOGIN FLOW
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text("Mobile Number (with +91)") },
                        placeholder = { Text("+919876543210") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        enabled = !isOtpSent
                    )

                    if (isOtpSent) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = otpText,
                            onValueChange = { otpText = it },
                            label = { Text("6-Digit OTP Code") },
                            placeholder = { Text("123456") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isOtpSent) {
                        Button(
                            onClick = {
                                if (phoneText.trim().isBlank()) {
                                    Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                                } else {
                                    isLoading = true
                                    FirebaseAuthManager.sendOtp(
                                        context = context,
                                        rawPhoneNumber = phoneText.trim(),
                                        onCodeSent = { _ ->
                                            isLoading = false
                                            isOtpSent = true
                                            Toast.makeText(context, "OTP Sent via SMS!", Toast.LENGTH_SHORT).show()
                                        },
                                        onVerificationFailed = { err ->
                                            isLoading = false
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        },
                                        onAutoVerified = {
                                            isLoading = false
                                            Toast.makeText(context, "Phone Verified!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess(FirebaseAuthManager.currentUser)
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Text("Send SMS OTP", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (otpText.isBlank()) {
                                    Toast.makeText(context, "Please enter OTP code", Toast.LENGTH_SHORT).show()
                                } else {
                                    isLoading = true
                                    FirebaseAuthManager.verifyOtp(
                                        otpCode = otpText,
                                        onSuccess = {
                                            isLoading = false
                                            Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess(FirebaseAuthManager.currentUser)
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Text("Verify OTP & Sign In", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // EMAIL & PASSWORD FLOW
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = { passwordText = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (emailText.isBlank() || passwordText.isBlank()) {
                                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                scope.launch {
                                    val res = if (isSignUpMode) {
                                        FirebaseAuthManager.signUpWithEmail(emailText.trim(), passwordText)
                                    } else {
                                        FirebaseAuthManager.signInWithEmail(emailText.trim(), passwordText)
                                    }
                                    isLoading = false
                                    res.onSuccess { user ->
                                        Toast.makeText(context, "Signed in as ${user.email}", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess(user)
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Auth Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text(if (isSignUpMode) "Create Account" else "Sign In", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                        Text(
                            text = if (isSignUpMode) "Already have an account? Sign In" else "New here? Create Account",
                            fontSize = 12.sp,
                            color = RoyalIndigo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onContinueOffline) {
                    Text("Continue Offline / Guest Mode", fontSize = 12.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
