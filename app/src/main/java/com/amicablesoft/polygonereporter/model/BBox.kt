package com.amicablesoft.polygonereporter.model

import com.google.android.gms.maps.model.LatLngBounds

data class BBox(val sw: Loc, val ne: Loc)

fun LatLngBounds.toBBox(): BBox {
    return BBox(southwest.toLoc(), northeast.toLoc())
}