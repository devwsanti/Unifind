package com.example.unifind.ui.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.unifind.R
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

/**
 * OOP Concept: Encapsulation
 * Encapsulates the gesture and transformation logic for zooming, panning, and two-finger rotating the map.
 */
@Composable
fun ZoomableMap(
    onMapTap: (mapX: Float, mapY: Float) -> Unit,
    modifier: Modifier = Modifier,
    focusTarget: Pair<Float, Float>? = null,
    hasSheet: Boolean = false,
    enableRotation: Boolean = true,
    content: @Composable BoxWithConstraintsScope.(scale: Float, offset: Offset, rotation: Float) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val rotationAnimatable = remember { Animatable(0f) }
    val rotation = rotationAnimatable.value
    val coroutineScope = rememberCoroutineScope()

    // The exact aspect ratio of the provided campus_map.png (941x1171)
    val mapAspectRatio = 941f / 1171f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(enableRotation) {
                detectTransformGestures { centroid, pan, zoom, rotationChange ->
                    val oldScale = scale
                    scale = (scale * zoom).coerceIn(1f, 6f)

                    if (enableRotation && rotationChange != 0f) {
                        coroutineScope.launch {
                            val nextRot = (rotationAnimatable.value + rotationChange) % 360f
                            rotationAnimatable.snapTo(nextRot)
                        }
                    }

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
            .pointerInput(enableRotation) {
                detectTapGestures { tapOffset ->
                    val (mapX, mapY) = MapCoordinates.screenToMap(
                        tapX = tapOffset.x,
                        tapY = tapOffset.y,
                        containerWidth = size.width.toFloat(),
                        containerHeight = size.height.toFloat(),
                        scale = scale,
                        offset = offset,
                        rotationDeg = rotation,
                        imageAspectRatio = mapAspectRatio
                    )
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

        LaunchedEffect(focusTarget, hasSheet) {
            if (focusTarget != null) {
                val (fx, fy) = focusTarget

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

                val sheetHeightPx = if (hasSheet) containerHeightPx * 0.3f else 0f
                val idealOffsetY = (scaledImageHeight / 2f) - targetPixelY - (sheetHeightPx / 2f)

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
                    rotationZ = rotation,
                    translationX = offset.x,
                    translationY = offset.y,
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.campus_map),
                contentDescription = "Campus Map",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                content(scale, offset, rotation)
            }
        }

        if (enableRotation && abs(rotation) > 0.5f) {
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        rotationAnimatable.animateTo(0f, animationSpec = tween(300))
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .semantics { contentDescription = "Reset North" },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Compass - North Up",
                    modifier = Modifier.graphicsLayer(rotationZ = -rotation)
                )
            }
        }
    }
}
