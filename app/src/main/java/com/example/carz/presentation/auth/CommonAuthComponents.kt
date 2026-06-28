package com.example.carz.presentation.auth

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsAndPrivacyText(color: Color) {
    val annotatedString = buildAnnotatedString {
        append("By using this app you agree to ")
        pushStringAnnotation(tag = "privacy", annotation = "privacy")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Privacy policies")
        }
        pop()
        append(" and ")
        pushStringAnnotation(tag = "terms", annotation = "terms")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Terms")
        }
        pop()
    }

    Text(
        text = annotatedString,
        color = color,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}
