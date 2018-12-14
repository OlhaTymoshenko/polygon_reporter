package com.amicablesoft.polygonereporter.ui

import androidx.annotation.StringRes
import com.amicablesoft.polygonereporter.model.Loc
import java.util.*

interface MainView {

    fun moveMapToLoc(loc: Loc, zoom: Float, withAnimation: Boolean)
    fun updateCurrentLocationMarker(loc: Loc)
    fun drawArea(areaPoints: ArrayList<Loc>, editMode: Boolean)
    fun editModeOn()
    fun editModeFinish(isInside: Boolean)

    fun showReportView()
    fun showDialog()
    fun showMessage(@StringRes message: Int)
    fun showError(@StringRes message: Int)
}