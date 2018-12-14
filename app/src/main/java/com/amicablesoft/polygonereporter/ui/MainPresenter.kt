package com.amicablesoft.polygonereporter.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import com.amicablesoft.polygonereporter.*
import com.amicablesoft.polygonereporter.model.BBox
import com.amicablesoft.polygonereporter.model.Loc
import com.amicablesoft.polygonereporter.model.toLoc
import com.amicablesoft.polygonereporter.utils.isPointInsidePolygon
import com.amicablesoft.polygonereporter.utils.isSegmentsIntersect
import com.google.android.gms.location.LocationServices
import rx.Subscription
import rx.subjects.BehaviorSubject
import java.util.*

class MainPresenter {

    lateinit var view: MainView
    lateinit var permissionManager: PermissionManager
    lateinit var context: Context
    lateinit var lastLoc: Loc

    private var mapPosition: BehaviorSubject<BBox> = BehaviorSubject.create()
    private var areaSubscription: Subscription? = null
    private var editAreaMode = false
    private var editArea = ArrayList<Loc>()

    private val mapReady: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private val DEFAULT_ZOOM = 14f

    fun onCreate() {
        if (!permissionManager.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else {
            showLastLocation()
        }
    }

    fun onDestroy() {
        areaSubscription?.unsubscribe()
    }

    fun locationPermissionGranted() {
        showLastLocation()
    }

    fun locationPermissionNotGranted() {
        view.showError(R.string.location_error__permission_not_granted)
        view.editModeFinish(false)
    }

    fun onMapPositionChanged(bBox: BBox) {
        mapPosition.onNext(bBox)
    }

    fun onMapReady() {
        mapReady.onNext(true)
    }

    fun onCreatePolygonAction() {
        view.showDialog()
    }

    fun onAreaStartEdit() {
        if (!editAreaMode) {
            editAreaMode = true
            view.showMessage(R.string.main__edit_polygon_message)
            view.editModeOn()
        }
    }

    fun onAreaAddPoint(point: Loc) {
        if (editAreaMode && canAddPoint(point)) {
            editArea.add(point)
            view.drawArea(editArea, true)
        }
    }

    fun onAreaEditFinish() {
        if (editAreaMode && canAddPoint(editArea.first())) {
            if (editArea.last() != editArea.first()) {
                editArea.add(editArea.first())
                view.drawArea(editArea, false)
                editAreaMode = false
                if (isPointInsidePolygon(lastLoc, editArea)) {
                    view.editModeFinish(true)
                } else {
                    view.editModeFinish(false)
                }
            } else {
                view.showError(R.string.main__error__area_invalid_shape)
            }
        }
    }

    fun onCreateReport() {
        view.showReportView()
    }

    @SuppressLint("MissingPermission")
    private fun showLastLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { it ->
            lastLoc = it.toLoc()
            view.moveMapToLoc(it.toLoc(), DEFAULT_ZOOM, true)
            view.updateCurrentLocationMarker(it.toLoc())
        }
    }

    private fun canAddPoint(point: Loc): Boolean {
        if (editArea.size > 1) {
            for (pointIdx in 0..(editArea.size - 2)) {
                val a = editArea[pointIdx]
                val b = editArea[pointIdx + 1]
                if (isSegmentsIntersect(
                        a,
                        b,
                        editArea.last(),
                        point
                    )
                ) {
                    return false
                }
            }
        }
        return true
    }
}