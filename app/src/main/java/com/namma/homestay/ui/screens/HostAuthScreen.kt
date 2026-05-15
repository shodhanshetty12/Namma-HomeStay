package com.namma.homestay.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.namma.homestay.ui.components.FeedbackSnackbar
import com.namma.homestay.ui.components.NammaTextField
import com.namma.homestay.ui.components.PrimaryButtonRow
import kotlinx.coroutines.tasks.await

@Composable
fun HostAuthScreen(
    onAuthSuccess: (String) -> Unit,
    onBack: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Host Login" else "Host Registration",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isLogin) "Welcome back to your homestay portal" else "Start your journey as a rural host",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                AnimatedVisibility(visible = !isLogin) {
                    NammaTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )
                }

                NammaTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    placeholder = "host@example.com"
                )

                NammaTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    placeholder = "Min 6 characters",
                    // Custom implementation for password visibility toggle
                )
                
                // Note: Standard OutlinedTextField is better for password toggle logic
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.Lock, null) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButtonRow(
                    primaryText = if (isLogin) "Login" else "Register",
                    onPrimaryClick = {
                        isLoading = true
                        errorMessage = null
                        val task = if (isLogin) {
                            auth.signInWithEmailAndPassword(email, password)
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                        }
                        
                        task.addOnCompleteListener { result ->
                            isLoading = false
                            if (result.isSuccessful) {
                                onAuthSuccess(auth.currentUser?.uid ?: "")
                            } else {
                                errorMessage = result.exception?.localizedMessage ?: "Authentication failed"
                            }
                        }
                    },
                    isLoading = isLoading
                )

                TextButton(
                    onClick = { isLogin = !isLogin },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (isLogin) "New Host? Create Account" else "Already have an account? Login")
                }
            }
        }
        
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("Go Back to Welcome Screen")
        }

        FeedbackSnackbar(
            message = errorMessage,
            onDismiss = { errorMessage = null },
            isError = true
        )
    }
}
