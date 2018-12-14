package com.amicablesoft.polygonereporter.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Loc(val lat:Double, val lng:Double): Serializable

fun Location.toLoc(): Loc =
    Loc(latitude, longitude)

fun LatLng.toLoc(): Loc =
    Loc(latitude, longitude)

fun Loc.toLatLng(): LatLng = LatLng(lat, lng)