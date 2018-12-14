package com.amicablesoft.polygonereporter.utils

import com.amicablesoft.polygonereporter.model.Loc
import java.util.ArrayList
import kotlin.Comparator

fun isSegmentsIntersect(segment1A: Loc, segment1B: Loc, segment2A: Loc, segment2B: Loc): Boolean {
    return findSegmentsIntersection(
        segment1A.lng, segment1A.lat,
        segment1B.lng, segment1B.lat,
        segment2A.lng, segment2A.lat,
        segment2B.lng, segment2B.lat
    ) != null
}

fun findSegmentsIntersection(ax:Double, ay:Double,
                             bx:Double, by:Double,
                             cx:Double, cy:Double,
                             dx:Double, dy:Double): Pair<Double, Double>? {
    //  Fail if either line segment is zero-length.
    if (ax == bx && ay == by || cx == dx && cy == dy) {
        return null
    }

    if (isSegmentsCollinear(ax, ay, bx, by, cx, cy, dx, dy)) {
        if (Math.min(ax, bx) <= cx && cx <= Math.max(ax, bx) && Math.min(
                ay,
                by
            ) <= cy && cy <= Math.max(ay, by)
        ) {
            return Pair(cx, cy)
        }

        if (Math.min(ax, bx) <= dx && dx <= Math.max(ax, bx) && Math.min(
                ay,
                by
            ) <= dy && dy <= Math.max(ay, by)
        ) {
            return Pair(dx, dy)
        }
    }

    //  Fail if the segments share an end-point.
    if (ax == cx && ay == cy || bx == cx && by == cy
        ||  ax == dx && ay == dy || bx == dx && by == dy) {
        return null
    }

    //  (1) Translate the system so that point A is on the origin.
    val nbx = bx - ax
    val nby = by - ay

    var ncx = cx - ax
    var ncy = cy - ay

    var ndx = dx - ax
    var ndy = dy - ay


    //  Discover the length of segment A-B.
    val distAB = Math.sqrt(nbx * nbx + nby * nby)

    //  (2) Rotate the system so that point B is on the positive X axis.
    val theCos = nbx/distAB
    val theSin = nby/distAB

    var newX = (ncx * theCos) + (ncy * theSin)
    ncy = (ncy * theCos) - (ncx * theSin)
    ncx = newX

    newX = (ndx * theCos) + (ndy * theSin)
    ndy = (ndy * theCos) - (ndx * theSin)
    ndx = newX

    //  Fail if segment C-D doesn't cross line A-B.
    if (ncy < 0.0 && ndy < 0.0 || ncy >= 0.0 && ndy >= 0.0) {
        return null
    }

    //  (3) Discover the position of the intersection point along line A-B.
    val abPos = ndx + (ncx - ndx) * ndy / (ndy - ncy)

    //  Fail if segment C-D crosses line A-B outside of segment A-B.
    if (abPos < 0.0 || abPos > distAB) {
        return null
    }

    val itrX = ax + abPos * theCos
    val itrY = ay + abPos * theSin

    return Pair(itrX, itrY)
}

fun isSegmentsCollinear(ax:Double, ay:Double,
                        bx:Double, by:Double,
                        cx:Double, cy:Double,
                        dx:Double, dy:Double): Boolean {

    val mf = (by - ay) / (bx - ax)
    val ms = (dy - cy) / (dx - cx)
    return mf == ms
}

fun isPointInsidePolygon(point: Loc, polygon: ArrayList<Loc>): Boolean {
    val rayPoint = findRayPoint(polygon)

    var intersectCount = 0
    for (pointIdx in 0..polygon.count() - 2) {
        if (isSegmentsIntersect(
                point,
                rayPoint,
                polygon[pointIdx],
                polygon[pointIdx + 1]
            )
        ) {
            intersectCount++
        }
    }

    return intersectCount % 2 == 1 // odd = inside, even = outside;
}

private fun findRayPoint(polygon: ArrayList<Loc>): Loc {
    val polygonMinX = polygon.minWith(Comparator { p1, p2 ->
        if (p1.lng < p2.lng)
            -1
        else if (p1.lng > p2.lng)
            1
        else
            0
    })!!.lng

    val polygonMaxX = polygon.minWith(Comparator { p1, p2 ->
        if (p1.lng < p2.lng)
            1
        else if (p1.lng > p2.lng)
            -1
        else
            0
    })!!.lng

    val epsilon = (polygonMaxX - polygonMinX) / 100f
    return Loc(polygonMinX, polygonMinX - epsilon)
}
