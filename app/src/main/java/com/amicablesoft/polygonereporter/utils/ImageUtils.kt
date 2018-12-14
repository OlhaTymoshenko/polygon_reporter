package com.amicablesoft.polygonereporter.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.amicablesoft.polygonereporter.BuildConfig
import java.io.File

fun getCameraIntent(packageManager: PackageManager, contentUri: Uri): Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    .let { intent ->
        return@let if (intent.resolveActivity(packageManager) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
            intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION and Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent
        } else {
            null
        }
    }

fun generateImageFile(context: Context): File {
    val photoPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
    photoPath.mkdirs()
    val fileName = "img_" + System.currentTimeMillis() + ".jpg"

    return File(photoPath, fileName)
}

fun getUriForFile(context: Context, photoFile: File): Uri {
    return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile)
}