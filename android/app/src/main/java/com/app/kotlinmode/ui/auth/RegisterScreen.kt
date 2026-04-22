package com.app.kotlinmode.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by viewModel.state.collectAsState()

    LaunchedEffect(authState) {
        if (authState is Resource.Success) {
            viewModel.resetState()
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkSurface, DarkBackground)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✦ KotlinMode", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, textAlign = TextAlign.Center)
            Text("Create your account", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(top = 6.dp, bottom = 36.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = BrandPrimary) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary, unfocusedBorderColor = TextMuted,
                    focusedLabelColor = BrandPrimary, cursorColor = BrandPrimary,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                ), singleLine = true
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = BrandPrimary) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary, unfocusedBorderColor = TextMuted,
                    focusedLabelColor = BrandPrimary, cursorColor = BrandPrimary,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = BrandPrimary) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = TextMuted)
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary, unfocusedBorderColor = TextMuted,
                    focusedLabelColor = BrandPrimary, cursorColor = BrandPrimary,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true
            )

            AnimatedVisibility(authState is Resource.Error) {
                Text((authState as? Resource.Error)?.message ?: "", color = ErrorRed, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.register(username.trim(), email.trim(), password) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandSecondary, disabledContainerColor = TextMuted),
                enabled = authState !is Resource.Loading
            ) {
                if (authState is Resource.Loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = TextPrimary, strokeWidth = 2.dp)
                else Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(20.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? ", color = TextSecondary, fontSize = 14.sp)
                Text("Log In", color = BrandPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
