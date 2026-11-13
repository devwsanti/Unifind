package com.example.unifind.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Facility
import com.example.unifind.model.Gate
import com.example.unifind.model.Location

/**
 * OOP Concept: Polymorphism
 * Renders different markers based on the Location subclass.
 */
@Composable
fun MapMarker(
    location: Location,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (location) {
        is Gate -> Color.Red
        is Facility -> Color.Green
        is Building -> {
            if (location.status == BuildingStatus.UNDER_CONSTRUCTION) {
                Color.Gray
            } else {
                Color.Blue
            }
        }
        else -> Color.Black
    }

    val baseSize = if (isSelected) 14.dp else 10.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer(
                scaleX = 1f / scale,
                scaleY = 1f / scale,
                transformOrigin = TransformOrigin(0.5f, 0.5f) // Centers the scale reversal on the offset
            )
    ) {
        Box(
            modifier = Modifier
                .size(baseSize)
                .clip(CircleShape)
                .background(color)
                .border(if (isSelected) 2.dp else 1.dp, Color.White, CircleShape)
                .clickable { onClick() }
        )
        if (scale >= 2.5f || isSelected) {
            Text(
                text = location.name,
                color = Color.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
