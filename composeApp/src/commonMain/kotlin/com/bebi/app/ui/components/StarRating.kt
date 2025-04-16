package com.bebi.app.ui.components

import androidx.compose.foundation.clickable
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

@Composable
fun StarRating(
    rating: Float,
    maxRating: Int = 10,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        for (i in 1..maxRating) {
            val isFilled = i <= rating
            
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $i",
                tint = if (isFilled) Color(0xFFFFD700) else Color.Gray, // Gold color for filled stars
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
                    .clickable {
                        // Set rating to exactly the star number (no half stars)
                        onRatingChanged(i.toFloat())
                    }
            )
        }
    }
}
