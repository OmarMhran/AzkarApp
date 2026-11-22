package com.example.azkarapp.presentation.azan

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azkarapp.ui.theme.goldPalette
import com.example.azkarapp.util.notification.AzanTester


@Composable
fun AzanTestCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val p = goldPalette()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = p.card),
        border = BorderStroke(1.dp, p.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "تجربة شاشة الأذان",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = p.text
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { AzanTester.previewScreen(context) },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, p.border)
            ) { Text("معاينة الشاشة الآن (من غير صوت)", color = p.accent) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (AzanTester.scheduleTest(context, delaySeconds = 10)) {
                        Toast.makeText(
                            context,
                            "هيشتغل الأذان بعد ١٠ ثواني. اقفل الشاشة دلوقتي.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = p.accent,
                    contentColor = p.onAccent
                )
            ) { Text("أذان تجريبي كامل بعد ١٠ ثواني") }
        }
    }
}