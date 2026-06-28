package com.example.carz.presentation.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carz.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun RegistrationScreen(
    onLoginSuccess: (String, String?) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        val user = auth.currentUser
                        onLoginSuccess(
                            user?.displayName ?: account.displayName ?: "User",
                            user?.photoUrl?.toString()
                        )
                    } else {
                        Toast.makeText(context, "Đăng nhập thất bại: ${signInTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Lỗi Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = "Enter your number",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3E50)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Phone Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFFF3F3F3), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.vietnam),
                contentDescription = "Vietnam Flag",
                modifier = Modifier.size(32.dp)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+84",
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                decorationBox = { innerTextField ->
                    if (phoneNumber.isEmpty()) {
                        Text(
                            text = "901234567",
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                if (phoneNumber.isNotEmpty()) {
                    onLoginSuccess("Mạnh Duy", null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AB2D6))
        ) {
            Text(text = "Select", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Google Sign-In Button
        SocialButton(
            text = "Continue with Google",
            iconRes = R.drawable.google,
            onClick = { 
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                
                // Fix: Force account picker by signing out first
                googleSignInClient.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SocialButton(
            text = "Continue with Facebook",
            iconRes = R.drawable.facebook,
            onClick = { /* Facebook logic */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text(text = "want to become a rider ? ", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "join us",
                color = Color(0xFF5AB2D6),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TermsAndPrivacyText(color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SocialButton(text: String, iconRes: Int?, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(text = text, color = Color.DarkGray, fontSize = 16.sp)
        }
    }
}

@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = { it() }
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        decorationBox = decorationBox
    )
}
