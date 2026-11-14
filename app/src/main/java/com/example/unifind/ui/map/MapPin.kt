package com.example.unifind.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Facility
import com.example.unifind.model.Gate
import com.example.unifind.model.Location

/**
 * OOP Concept: Polymorphism / Composition
 * A custom Map Pin component representing different types of Locations.
 */
@Composable
fun MapPin(
    location: Location,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditorMode: Boolean = false
) {
    val color = when (location) {
        is Gate -> Color.Red
        is Facility -> Color.Green
        is Building -> {
            if (location.status == BuildingStatus.UNDER_CONSTRUCTION) Color.Gray else Color.Blue
        }
        else -> Color.Black
    }

    MapPinBase(
        color = color,
        label = location.name,
        scale = scale,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier,
        isEditorMode = isEditorMode
    )
}

@Composable
fun MapPinBase(
    color: Color,
    label: String,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditorMode: Boolean = false
) {
    val heightDp = if (isSelected) 31.dp else 24.dp
    val widthDp = heightDp * 0.7f
    val alpha = if (isEditorMode) 0.5f else 1f

    // Wrapper anchoring the map-coordinate point 1:1 on layout
    Box(modifier = modifier) {
        // The interactive pin body offsetting to center the tip down appropriately.
        Box(
            modifier = Modifier
                .offset(x = -widthDp / 2, y = -heightDp)
                .size(widthDp, heightDp)
                .let { if (!isEditorMode) it.clickable { onClick() } else it }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = alpha)) {
                val w = size.width
                val h = size.height

                val path = Path().apply {
                    moveTo(w / 2f, h)
                    quadraticBezierTo(0f, h * 0.6f, 0f, w / 2f)
                    arcTo(
                        rect = Rect(0f, 0f, w, w),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    quadraticBezierTo(w, h * 0.6f, w / 2f, h)
                    close()
                }

                drawPath(path = path, color = color)
                drawPath(path = path, color = Color.White, style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = Color.White, radius = w / 5f, center = Offset(w / 2f, w / 2f))

                if (isSelected) {
                    drawCircle(
                        color = color,
                        radius = w / 1.5f,
                        center = Offset(w / 2f, h),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        if (scale >= 2.5f || isSelected) {
            Text(
                text = label,
                color = Color.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .offset(x = widthDp / 2 + 4.dp, y = -heightDp * 0.75f)
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
