package com.amicablesoft.polygonereporter.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.amicablesoft.polygonereporter.*
import com.amicablesoft.polygonereporter.model.Loc
import com.amicablesoft.polygonereporter.model.toBBox
import com.amicablesoft.polygonereporter.model.toLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), MainView,
    PermissionManager, OnMapReadyCallback {

    private lateinit var presenter: MainPresenter

    private var map: GoogleMap? = null
    private var currentLocationMarker: Marker? = null
    private var dialog: AlertDialog? = null
    private var lastLoc: Loc? = null

    private val PERMISSIONS_REQUEST__LOCATION = 100

    var areaMarker:BitmapDescriptor? = null
    var areaStrokeWidth:Int = 0
    var areaStrokeColor:Int = Color.BLACK
    var areaFillColor:Int = Color.TRANSPARENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fab_create.setOnClickListener { presenter.onCreatePolygonAction() }
        fab_finish.setOnClickListener { presenter.onAreaEditFinish() }
        fab_report.setOnClickListener { presenter.onCreateReport() }

        val markerDrawable = ResourcesCompat.getDrawable(resources,
            R.drawable.area_marker, theme)!!
        val markerBitmap = Bitmap.createBitmap(markerDrawable.intrinsicWidth, markerDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(markerBitmap)
        markerDrawable.setBounds(0, 0, canvas.width, canvas.height)
        markerDrawable.draw(canvas)

        areaMarker = BitmapDescriptorFactory.fromBitmap(markerBitmap)
        areaStrokeColor = ResourcesCompat.getColor(resources,
            R.color.map_polygon_stroke_blue, theme)
        areaFillColor = ResourcesCompat.getColor(resources,
            R.color.map_polygon_fill_blue, theme)
        areaStrokeWidth = resources.getDimensionPixelSize(R.dimen.map_polygon_stride_width)

        initPresenter()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
        dialog?.dismiss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST__LOCATION ->  {
                when {
                    grantResults.isEmpty() -> {
                        // If user interaction was interrupted, the permission request is cancelled and you
                        // receive empty arrays.
                    }
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> presenter.locationPermissionGranted()
                    else -> presenter.locationPermissionNotGranted()
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //
    //MainViewImpl
    //
    override fun moveMapToLoc(loc: Loc, zoom: Float, withAnimation: Boolean) {
        val cu = CameraUpdateFactory.newLatLngZoom(loc.toLatLng(), zoom)
        if (withAnimation) {
            map?.animateCamera(cu)
        }
        else {
            map?.moveCamera(cu)
        }
    }

    override fun updateCurrentLocationMarker(loc: Loc) {
        lastLoc = loc
        if (currentLocationMarker == null) {
            currentLocationMarker = map?.addMarker(
                MarkerOptions().position(loc.toLatLng()))
        }
        else {
            currentLocationMarker?.position = loc.toLatLng()
        }
    }

    override fun drawArea(areaPoints: ArrayList<Loc>, editMode: Boolean) {
        map?.clear()

        if (areaPoints.size == 0)
            return

        map?.addMarker(MarkerOptions().position(lastLoc!!.toLatLng()))
        val points = areaPoints.map { it -> LatLng(it.lat, it.lng) }
        if (editMode) {
            val polylineOps = PolylineOptions()
            polylineOps.addAll(points)
            polylineOps.color(areaStrokeColor)
            polylineOps.width(areaStrokeWidth.toFloat())
            polylineOps.clickable(true)
            map?.addPolyline(polylineOps)
            for (point in points) {
                map?.addMarker(MarkerOptions()
                    .position(point)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(areaMarker))
            }
        }
        else {
            val polygonOps = PolygonOptions()
            polygonOps.addAll(points)
            polygonOps.strokeColor(areaStrokeColor)
            polygonOps.strokeWidth(areaStrokeWidth.toFloat())
            polygonOps.fillColor(areaFillColor)
            map?.addPolygon(polygonOps)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun editModeOn() {
        fab_create.visibility = View.INVISIBLE
        fab_finish.visibility = View.VISIBLE
        fab_report.visibility = View.INVISIBLE
    }

    @SuppressLint("RestrictedApi")
    override fun editModeFinish(isInside: Boolean) {
        if (isInside) {
            fab_create.visibility = View.INVISIBLE
            fab_finish.visibility = View.INVISIBLE
            fab_report.visibility = View.VISIBLE
        } else {
            fab_create.visibility = View.INVISIBLE
            fab_finish.visibility = View.INVISIBLE
            fab_report.visibility = View.INVISIBLE
        }
    }

    override fun showReportView() {
        val intent = Intent(this, ReportActivity::class.java)
        startActivity(intent)
    }

    override fun showDialog() {
        dialog = AlertDialog.Builder(this)
            .setMessage(R.string.main__dialog_message)
            .setPositiveButton(R.string.main__dialog_positive_button) { _, _ -> presenter.onAreaStartEdit() }
            .setNegativeButton(R.string.main__dialog_negative_button) { _, _ -> }
            .create()
        dialog?.show()
    }

    override fun showMessage(message: Int) {
        Snackbar.make(findViewById(R.id.activity_main), message, Snackbar.LENGTH_LONG).show()
    }

    override fun showError(message: Int) {
        Snackbar.make(findViewById(R.id.activity_main), message, Snackbar.LENGTH_LONG).show()
    }

    //
    //PermissionManagerImpl
    //
    override fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED


    override fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSIONS_REQUEST__LOCATION)
    }

    //
    //OnMapReadyCallbackImpl
    //
    override fun onMapReady(p0: GoogleMap?) {
        map = p0

        map?.setOnCameraIdleListener {
            presenter.onMapPositionChanged(p0?.projection?.visibleRegion?.latLngBounds?.toBBox()!!)
        }
        map?.setOnMapClickListener { point ->
            presenter.onAreaAddPoint(
                Loc(
                    point.latitude,
                    point.longitude
                )
            )
        }
        presenter.onMapReady()
    }

    private fun initPresenter() {
        presenter = MainPresenter()
        presenter.view = this
        presenter.permissionManager = this
        presenter.context = this
        presenter.onCreate()
    }
}
