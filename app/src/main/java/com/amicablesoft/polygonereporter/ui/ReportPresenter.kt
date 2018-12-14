package com.amicablesoft.polygonereporter.ui

import android.content.Context
import android.net.Uri
import com.amicablesoft.polygonereporter.utils.generateImageFile
import java.io.File

class ReportPresenter {

    lateinit var view: ReportView
    lateinit var context: Context

    private var photo: File? = null

    fun onOpenCamera() {
        photo = null
        view.showCamera()
    }

    fun getAvatarFile(): File {
        if (photo == null) {
            photo = generateImageFile(context)
        }
        return photo!!
    }

    fun onPhotoUpdate() {
        if (photo != null) {
            view.updatePhoto(Uri.fromFile(photo))
        }
    }

    fun onPickDate() {
        view.showDatePickerDialog()
    }
}