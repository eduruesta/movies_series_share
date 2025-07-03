package com.bebi.watchit.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Arrow_back: ImageVector
    get() {
        if (_Arrow_left_alt != null) return _Arrow_left_alt!!

        _Arrow_left_alt = ImageVector.Builder(
            name = "Arrow_left_alt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(400f, 720f)
                lineTo(160f, 480f)
                lineToRelative(240f, -240f)
                lineToRelative(56f, 58f)
                lineToRelative(-142f, 142f)
                horizontalLineToRelative(486f)
                verticalLineToRelative(80f)
                horizontalLineTo(314f)
                lineToRelative(142f, 142f)
                close()
            }
        }.build()

        return _Arrow_left_alt!!
    }

private var _Arrow_left_alt: ImageVector? = null

