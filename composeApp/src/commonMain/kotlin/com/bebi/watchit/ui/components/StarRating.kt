package com.bebi.watchit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A star rating component that can be editable or read-only
 * If onRatingChanged is provided, the stars will be clickable
 */
@Composable
fun StarRating(
    rating: Float,
    maxRating: Int = 10,
    modifier: Modifier = Modifier,
    onRatingChanged: ((Float) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..maxRating) {
            val isFilled = i <= rating
            
            val starModifier = if (onRatingChanged != null) {
                Modifier
                    .size(24.dp)
                    .padding(horizontal = 2.dp)
                    .clickable {
                        onRatingChanged(i.toFloat())
                    }
            } else {
                Modifier
                    .size(24.dp)
                    .padding(horizontal = 2.dp)
            }
            
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $i",
                tint = if (isFilled) Color(0xFFFFD700) else Color.Gray,
                modifier = starModifier
            )
        }
    }
}
