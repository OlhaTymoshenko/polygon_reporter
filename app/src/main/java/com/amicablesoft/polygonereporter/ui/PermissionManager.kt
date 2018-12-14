package com.amicablesoft.polygonereporter.ui

interface PermissionManager {

    fun isPermissionGranted(permission: String): Boolean
    fun requestPermission(permission: String)
}