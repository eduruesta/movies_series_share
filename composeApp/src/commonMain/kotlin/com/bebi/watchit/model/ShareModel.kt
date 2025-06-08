package com.bebi.watchit.model

enum class MimeType {
    PDF,
    TEXT,
    IMAGE,
}

class ShareFileModel(
    val mime: MimeType = MimeType.PDF,
    val fileName: String,
    val bytes: ByteArray
)