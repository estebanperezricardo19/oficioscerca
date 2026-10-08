package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServiceStatus
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusAccepted
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusInProgress
import com.example.ui.theme.StatusOnTheWay
import com.example.ui.theme.StatusPending
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Primary Brand Action Button
 */
@Composable
fun OficiosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = SecondaryTeal,
    contentColor: Color = SurfaceWhite,
    testTag: String = "oficios_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFFBDD2D6),
            disabledContentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Secondary Outlined Button
 */
@Composable
fun OficiosOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "oficios_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, SecondaryTeal),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SecondaryTeal
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Status Badge for Service States
 */
@Composable
fun StatusBadge(
    status: ServiceStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        ServiceStatus.PENDING -> Pair(Color(0xFFFEF3C7), StatusPending)
        ServiceStatus.ACCEPTED -> Pair(Color(0xFFE0F2FE), StatusAccepted)
        ServiceStatus.ON_THE_WAY -> Pair(Color(0xFFE0F7FA), StatusOnTheWay)
        ServiceStatus.IN_PROGRESS -> Pair(Color(0xFFDBEAFE), StatusInProgress)
        ServiceStatus.COMPLETED -> Pair(Color(0xFFD1FAE5), StatusCompleted)
        ServiceStatus.CANCELLED -> Pair(Color(0xFFFEE2E2), StatusCancelled)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            text = status.labelEs,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Rating Stars component (Interactive or read-only)
 */
@Composable
fun RatingStars(
    rating: Double,
    maxStars: Int = 5,
    starSize: Dp = 18.dp,
    interactive: Boolean = false,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            val icon = if (isFilled) Icons.Default.Star else Icons.Outlined.StarBorder
            val tint = if (isFilled) StarGold else BorderSubtle

            Icon(
                imageVector = icon,
                contentDescription = "$i estrellas",
                tint = tint,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (interactive && onRatingChanged != null) {
                            Modifier
                                .clip(CircleShape)
                                .clickable { onRatingChanged(i) }
                                .padding(2.dp)
                        } else Modifier
                    )
            )
        }
    }
}

/**
 * Avatar with initial or badge
 */
@Composable
fun UserAvatar(
    initial: String,
    size: Dp = 48.dp,
    backgroundColor: Color = PrimaryNavy,
    textColor: Color = AccentCyan,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.uppercase(),
            color = textColor,
            fontSize = (size.value * 0.42).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Empty state container
 */
@Composable
fun EmptyStateView(
    title: String,
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = Color(0xFFD8EEF3)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "📂",
                    fontSize = 32.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OficiosButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.width(180.dp)
            )
        }
    }
}
