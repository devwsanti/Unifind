package com.example.unifind.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.example.unifind.R
import kotlin.math.max

/**
 * OOP Concept: Encapsulation
 * Encapsulates the gesture and transformation logic for zooming and panning the map.
 */
@Composable
fun ZoomableMap(
    onMapTap: (mapX: Float, mapY: Float) -> Unit,
    modifier: Modifier = Modifier,
    focusTarget: Pair<Float, Float>? = null,
    content: @Composable BoxWithConstraintsScope.(scale: Float) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // The exact aspect ratio of the provided campus_map.png (941x1171)
    val mapAspectRatio = 941f / 1171f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    scale = (scale * zoom).coerceIn(1f, 6f)

                    val containerWidthPx = size.width.toFloat()
                    val containerHeightPx = size.height.toFloat()
                    val containerRatio = containerWidthPx / containerHeightPx

                    val unscaledImageWidthPx: Float
                    val unscaledImageHeightPx: Float

                    if (containerRatio > mapAspectRatio) {
                        unscaledImageHeightPx = containerHeightPx
                        unscaledImageWidthPx = containerHeightPx * mapAspectRatio
                    } else {
                        unscaledImageWidthPx = containerWidthPx
                        unscaledImageHeightPx = containerWidthPx / mapAspectRatio
                    }

                    val scaledImageWidth = unscaledImageWidthPx * scale
                    val scaledImageHeight = unscaledImageHeightPx * scale

                    val maxX = max(0f, (scaledImageWidth - size.width) / 2f)
                    val maxY = max(0f, (scaledImageHeight - size.height) / 2f)

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val fraction = scale / oldScale
                    val offsetAdjustment = (centroid - center) * (1f - fraction)

                    val newOffset = offset + pan + offsetAdjustment

                    offset = Offset(
                        x = newOffset.x.coerceIn(-maxX, maxX),
                        y = newOffset.y.coerceIn(-maxY, maxY)
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val containerWidthPx = size.width.toFloat()
                    val containerHeightPx = size.height.toFloat()
                    val containerRatio = containerWidthPx / containerHeightPx

                    val unscaledImageWidthPx: Float
                    val unscaledImageHeightPx: Float

                    if (containerRatio > mapAspectRatio) {
                        unscaledImageHeightPx = containerHeightPx
                        unscaledImageWidthPx = containerHeightPx * mapAspectRatio
                    } else {
                        unscaledImageWidthPx = containerWidthPx
                        unscaledImageHeightPx = containerWidthPx / mapAspectRatio
                    }

                    val scaledImageWidth = unscaledImageWidthPx * scale
                    val scaledImageHeight = unscaledImageHeightPx * scale

                    val scaledTopLeftX = (size.width - scaledImageWidth) / 2f + offset.x
                    val scaledTopLeftY = (size.height - scaledImageHeight) / 2f + offset.y

                    val tapXOnImage = tapOffset.x - scaledTopLeftX
                    val tapYOnImage = tapOffset.y - scaledTopLeftY

                    val mapX = (tapXOnImage / scaledImageWidth).coerceIn(0f, 1f)
                    val mapY = (tapYOnImage / scaledImageHeight).coerceIn(0f, 1f)

                    onMapTap(mapX, mapY)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val widthDp = maxWidth
        val heightDp = maxHeight
        val containerRatio = widthDp / heightDp

        val unscaledWidthDp: Dp
        val unscaledHeightDp: Dp

        if (containerRatio > mapAspectRatio) {
            unscaledHeightDp = heightDp
            unscaledWidthDp = heightDp * mapAspectRatio
        } else {
            unscaledWidthDp = widthDp
            unscaledHeightDp = widthDp / mapAspectRatio
        }

        LaunchedEffect(focusTarget) {
            if (focusTarget != null) {
                val (fx, fy) = focusTarget

                // Zoom in
                scale = 3f

                // Wait for the next frame so layout sizes are correct, but roughly compute it
                val containerWidthPx = constraints.maxWidth.toFloat()
                val containerHeightPx = constraints.maxHeight.toFloat()
                val cRatio = containerWidthPx / containerHeightPx

                val unscaledImageWidthPx: Float
                val unscaledImageHeightPx: Float

                if (cRatio > mapAspectRatio) {
                    unscaledImageHeightPx = containerHeightPx
                    unscaledImageWidthPx = containerHeightPx * mapAspectRatio
                } else {
                    unscaledImageWidthPx = containerWidthPx
                    unscaledImageHeightPx = containerWidthPx / mapAspectRatio
                }

                val scaledImageWidth = unscaledImageWidthPx * scale
                val scaledImageHeight = unscaledImageHeightPx * scale

                val maxX = max(0f, (scaledImageWidth - containerWidthPx) / 2f)
                val maxY = max(0f, (scaledImageHeight - containerHeightPx) / 2f)

                val targetPixelX = fx * scaledImageWidth
                val targetPixelY = fy * scaledImageHeight

                val idealOffsetX = (scaledImageWidth / 2f) - targetPixelX
                val idealOffsetY = (scaledImageHeight / 2f) - targetPixelY

                offset = Offset(
                    x = idealOffsetX.coerceIn(-maxX, maxX),
                    y = idealOffsetY.coerceIn(-maxY, maxY)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(unscaledWidthDp, unscaledHeightDp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.campus_map),
                contentDescription = "Campus Map",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            // Provide exact box bounds to the content so offset calculations match mapX and mapY precisely
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                content(scale)
            }
        }
    }
}
