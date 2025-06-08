package com.bebi.watchit

import androidx.compose.runtime.Composable
import com.bebi.watchit.model.ShareFileModel

expect class ShareManager {
    fun shareText(text: String)
    suspend fun shareFile(file: ShareFileModel): Result<Unit>
}

@Composable
expect fun rememberShareManager(): ShareManager