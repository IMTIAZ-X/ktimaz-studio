package com.ktimazstudio.agent.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.agent.data.AppSettings
import com.ktimazstudio.agent.data.AppTheme
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@Composable
fun PlanSettings(viewModel: AgentViewModel, settings: AppSettings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Current Plan Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (settings.isProUser) {
                    AppTheme.ProStart.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (settings.isProUser) "Pro Plan" else "Free Plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    if (settings.isProUser) {
                        Spacer(Modifier.width(8.dp))
                        ProBadge()
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (settings.isProUser)
                        "You have access to all premium features"
                    else
                        "Upgrade to unlock unlimited APIs and all AI modes",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Toggle Plan Button (for demo purposes)
        Button(
            onClick = { viewModel.toggleProPlan(!settings.isProUser) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (settings.isProUser) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Icon(
                if (settings.isProUser) Icons.Default.KeyboardArrowDown else Icons.Default.Rocket,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (settings.isProUser) "Downgrade to Free (Demo)" else "Upgrade to Pro",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Pro Features",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Features List
        val features = listOf(
            "Unlimited API configurations (vs 5 limit)" to Icons.Default.CloudQueue,
            "Unlimited chat conversations" to Icons.Default.ChatBubble,
            "Unlimited file uploads (vs 10 limit)" to Icons.Default.CloudUpload,
            "All AI modes unlocked" to Icons.Default.Psychology,
            "Up to 5 APIs working simultaneously" to Icons.Default.GroupWork,
            "Thinking Mode - Deep reasoning" to Icons.Default.Lightbulb,
            "Research Mode - Academic analysis" to Icons.Default.Science,
            "Study Mode - Learning assistant" to Icons.Default.School,
            "Code Mode - Programming helper" to Icons.Default.Code,
            "Creative Mode - Writing assistant" to Icons.Default.Create,
            "Priority support" to Icons.Default.Support,
            "Advanced analytics" to Icons.Default.Analytics
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            features.forEach { (feature, icon) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(feature)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Free Plan Limitations Info Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Free Plan: 5 API configs max, 10 attachments per message, Standard mode only",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun ProBadge() {
    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(listOf(AppTheme.ProStart, AppTheme.ProEnd)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("PRO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
    }
}