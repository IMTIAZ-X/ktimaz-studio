package com.ktimazstudio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Inline Icon Composables ---

/**
 * Composable for a success checkmark icon.
 * Draws a checkmark using Canvas and Path.
 * @param tint Color to apply to the checkmark.
 * @param modifier Modifier for the icon.
 */
@Composable
fun SuccessCheckmarkIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 8f // Adjust stroke width as needed
        drawPath(
            path = Path().apply {
                // Path for the checkmark (adjust coordinates for desired shape)
                moveTo(size.width * 0.25f, size.height * 0.5f)
                lineTo(size.width * 0.45f, size.height * 0.7f)
                lineTo(size.width * 0.75f, size.height * 0.3f)
            },
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Composable for a phone call icon.
 * Draws a phone icon using Canvas and Path.
 * @param tint Color to apply to the phone icon.
 * @param modifier Modifier for the icon.
 */
@Composable
fun CallIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2f // Adjust stroke width as needed
        drawPath(
            path = Path().apply {
                // Path for a simple phone icon
                moveTo(size.width * 0.1f, size.height * 0.3f)
                lineTo(size.width * 0.3f, size.height * 0.1f)
                lineTo(size.width * 0.9f, size.height * 0.7f)
                lineTo(size.width * 0.7f, size.height * 0.9f)
                close()
                // Receiver part (optional, can be more detailed)
                moveTo(size.width * 0.2f, size.height * 0.2f)
                lineTo(size.width * 0.8f, size.height * 0.8f)
            },
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // A more simplified phone shape for clarity
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.7f)
                cubicTo(size.width * 0.1f, size.height * 0.8f, size.width * 0.2f, size.height * 0.9f, size.width * 0.3f, size.height * 0.9f)
                lineTo(size.width * 0.7f, size.height * 0.9f)
                cubicTo(size.width * 0.8f, size.height * 0.9f, size.width * 0.9f, size.height * 0.8f, size.width * 0.9f, size.height * 0.7f)
                lineTo(size.width * 0.9f, size.height * 0.3f)
                cubicTo(size.width * 0.9f, size.height * 0.2f, size.width * 0.8f, size.height * 0.1f, size.width * 0.7f, size.height * 0.1f)
                lineTo(size.width * 0.3f, size.height * 0.1f)
                cubicTo(size.width * 0.2f, size.height * 0.1f, size.width * 0.1f, size.height * 0.2f, size.width * 0.1f, size.height * 0.3f)
                close()
            },
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Draw a simple phone icon that looks like a phone
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.8f)
                lineTo(size.width * 0.8f, size.height * 0.8f)
                arcToRelative(0.1f * size.width, 0.1f * size.height, 0f, false, true, 0.1f * size.width, -0.1f * size.height)
                lineTo(size.width * 0.9f, size.height * 0.2f)
                arcToRelative(0.1f * size.width, 0.1f * size.height, 0f, false, true, -0.1f * size.width, -0.1f * size.height)
                lineTo(size.width * 0.2f, size.height * 0.1f)
                arcToRelative(0.1f * size.width, 0.1f * size.height, 0f, false, true, -0.1f * size.width, 0.1f * size.height)
                lineTo(size.width * 0.1f, size.height * 0.7f)
                arcToRelative(0.1f * size.width, 0.1f * size.height, 0f, false, true, 0.1f * size.width, 0.1f * size.height)
                close()
                // Receiver line
                moveTo(size.width * 0.3f, size.height * 0.2f)
                lineTo(size.width * 0.7f, size.height * 0.2f)
            },
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Composable for a message icon.
 * Draws a message bubble icon using Canvas and Path.
 * @param tint Color to apply to the message icon.
 * @param modifier Modifier for the icon.
 */
@Composable
fun MessageIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2f // Adjust stroke width as needed
        drawPath(
            path = Path().apply {
                // Message bubble shape
                moveTo(size.width * 0.1f, size.height * 0.2f)
                lineTo(size.width * 0.9f, size.height * 0.2f)
                arcToRelative(0.1f * size.width, 0.1f * size.height, 0f, false, true, 0.1f * size.width, 0.1f * size.height)
                lineTo(size.width, size.height * 0.7f)
                arcToRelative(0.1f * size.width, 0.1f * size.height, 0f, false, true, -0.1f * size.width, 0.1f * size.height)
                lineTo(size.width * 0.6f, size.height * 0.8f) // Tail base
                lineTo(size.width * 0.5f, size.height * 0.9f) // Tail tip
                lineTo(size.width * 0.4f, size.height * 0.8f) // Tail base
                lineTo(size.width * 0.1f, size.height * 0.8f)
                arcToRelative(0.1f * size.width, 0.1f * size.height, 0f, false, true, -0.1f * size.width, -0.1f * size.height)
                close()
            },
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

// --- Main Screen Composable ---

@Composable
fun SendMoneySuccessScreen() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Success Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)), // Light grey background for the circle
                contentAlignment = Alignment.Center
            ) {
                SuccessCheckmarkIcon(
                    tint = Color(0xFF4CAF50), // Green color for success
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. "সেন্ড মানি সফল" (Send Money Successful) Title
            Text(
                text = "সেন্ড মানি সফল",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Recipient Info
            Text(
                text = "Riman",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "01700000009",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Transaction Details (using a Column for vertical arrangement of rows)
            Column(
                modifier = Modifier.fillMaxWidth(0.8f) // Constrain width for details
            ) {
                TransactionDetailRow(label = "ট্রানজেকশন আইডি", value = "VBNJZKFO")
                TransactionDetailRow(label = "পরিমাণ", value = "577 টাকা")
                TransactionDetailRow(label = "খরচ", value = "5 টাকা")
                TransactionDetailRow(label = "সর্বমোট", value = "582 টাকা", isBold = true)
                TransactionDetailRow(label = "সময়", value = "01:12 PM 2024-04-03")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Action Buttons (Call and Message)
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Button
                TextButton(onClick = { /* TODO: Handle call */ }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CallIcon(
                            tint = MaterialTheme.colorScheme.primary, // Use primary color for icons
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "কল", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Spacer to separate buttons (or use Arrangement.SpaceAround)
                Spacer(modifier = Modifier.width(16.dp))

                // Message Button
                TextButton(onClick = { /* TODO: Handle message */ }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MessageIcon(
                            tint = MaterialTheme.colorScheme.primary, // Use primary color for icons
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "মেসেজ", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. Main "Home" Button
            Button(
                onClick = { /* TODO: Navigate to Home */ },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium, // Or RoundedCornerShape(12.dp)
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7511B)) // Nagad Orange
            ) {
                Text(
                    text = "হোম এ ফিরে যান",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TransactionDetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSendMoneySuccessScreen() {
    MaterialTheme { // Use your app's theme here
        SendMoneySuccessScreen()
    }
}