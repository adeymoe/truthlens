package uk.ac.tees.mad.e4615842.utils

import android.content.Context
import android.net.Uri
import android.util.Base64

fun uriToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bytes = inputStream?.readBytes()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}