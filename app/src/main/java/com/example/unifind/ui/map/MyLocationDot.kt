package com.example.unifind.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * OOP Concept: Composition
 * Renders the "You are here" user location indicator at constant visual size on the map canvas.
 */
@Composable
fun MyLocationDot(
    scale: Float,
    modifier: Modifier = Modifier,
    label: String = "You are here",
    nearPlace: String? = null,
    rotation: Float = 0f
) {
    val outerRadiusDp = 20.dp
    val innerRadiusDp = 8.dp
    val strokeWidthDp = 3.dp
    val totalSizeDp = outerRadiusDp * 2

    val fullDesc = if (!nearPlace.isNullOrBlank()) "$label - near $nearPlace" else label

    Box(
        modifier = modifier.semantics {
            contentDescription = fullDesc
        }
    ) {
        Box(
            modifier = Modifier
                .offset(x = -outerRadiusDp, y = -outerRadiusDp)
                .size(totalSizeDp)
                .graphicsLayer(
                    scaleX = 1f / scale,
                    scaleY = 1f / scale,
                    rotationZ = -rotation,
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(totalSizeDp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerPx = outerRadiusDp.toPx()
                val innerPx = innerRadiusDp.toPx()
                val strokePx = strokeWidthDp.toPx()

                // Translucent blue circle
                drawCircle(
                    color = Color(0x332196F3),
                    radius = outerPx,
                    center = center
                )

                // White border
                drawCircle(
                    color = Color.White,
                    radius = innerPx + strokePx,
                    center = center
                )

                // Solid blue dot inside
                drawCircle(
                    color = Color(0xFF1976D2),
                    radius = innerPx,
                    center = center
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = -(outerRadiusDp + 18.dp))
                    .wrapContentWidth(unbounded = true)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = label,
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (!nearPlace.isNullOrBlank()) {
                    Text(
                        text = " - near $nearPlace",
                        color = Color(0xFF424242),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
