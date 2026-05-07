package com.example.travelapp.view

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.example.travelapp.R
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: (FirebaseUser) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    // Google Sign-In client
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Google launcher
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModel.signInWithCredential(credential, onAuthSuccess)
        } catch (e: ApiException) {
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B2A), Color(0xFF25485E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // title
            Text(strings.appName, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                if (uiState.isLogin) strings.welcomeBack else strings.startJourney,
                fontSize = 14.sp, color = Color(0xFF8ECAE6)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle tabs
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF162032))
                    .padding(4.dp)
            ) {
                listOf(strings.signIn to true, strings.signUp to false).forEach { (label, value) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (uiState.isLogin == value) Color(0xFF219EBC) else Color.Transparent)
                            .clickable { viewModel.onTabSwitch(value) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            //name for create option
            AnimatedVisibility(visible = !uiState.isLogin) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(strings.enterName, color = Color(0xFF8ECAE6)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Email field
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(strings.email, color = Color(0xFF8ECAE6)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password field
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(strings.password, color = Color(0xFF8ECAE6)) },
                singleLine = true,
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = viewModel::onPasswordVisibilityToggle) {
                        Text(
                            if (uiState.passwordVisible) strings.hide else strings.show,
                            color = Color(0xFF8ECAE6), fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            // Error message
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main action button
            Button(
                onClick = { viewModel.signInOrRegister(onAuthSuccess) },
                enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.isLogin) strings.signIn else strings.createAccount,
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2A4A5E))
                Text(" " + strings.or + " ", color = Color(0xFF8ECAE6), fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2A4A5E))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Sign-In button
            OutlinedButton(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2A4A5E)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(strings.continueWithGoogle, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            // Forgot password (login only)
            AnimatedVisibility(visible = uiState.isLogin) {
                TextButton(onClick = {
                    viewModel.sendPasswordReset(
                        onSent = {
                            Toast.makeText(context, strings.resetEmailSent, Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(context, strings.couldNotSendReset, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text(strings.forgotPassword, color = Color(0xFF8ECAE6), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF219EBC),
    unfocusedBorderColor = Color(0xFF2A4A5E),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF219EBC)
)