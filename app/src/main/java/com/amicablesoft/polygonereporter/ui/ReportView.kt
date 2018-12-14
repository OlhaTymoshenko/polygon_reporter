package com.amicablesoft.polygonereporter.ui

import android.net.Uri

interface ReportView {

    fun showCamera()
    fun updatePhoto(uri: Uri)
    fun showDatePickerDialog()
}