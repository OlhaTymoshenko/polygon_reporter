package com.amicablesoft.polygonereporter.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.amicablesoft.polygonereporter.R
import com.amicablesoft.polygonereporter.utils.getCameraIntent
import com.amicablesoft.polygonereporter.utils.getDateFormat
import com.amicablesoft.polygonereporter.utils.getUriForFile
import com.amicablesoft.polygonereporter.utils.isValid
import kotlinx.android.synthetic.main.activity_report.*
import java.util.*

class ReportActivity: AppCompatActivity(), ReportView {

    companion object {
        private const val TAKE_IMAGE_CAMERA = 200
    }

    private lateinit var presenter: ReportPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        camera_image_view.setOnClickListener { presenter.onOpenCamera() }
        date_edit_text.setText(getDateFormat(Date()))
        date_edit_text.setOnClickListener {presenter.onPickDate() }

        initPresenter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.report_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_done -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                TAKE_IMAGE_CAMERA -> {
                    val avatarFile = presenter.getAvatarFile()
                    presenter.onPhotoUpdate()
                }
            }
        }
    }

    //
    //ReportViewImpl
    //
    override fun showCamera() {
        val takePhoto = getCameraIntent(
            packageManager,
            getUriForFile(this, presenter.getAvatarFile())
        )
        startActivityForResult(takePhoto,
            TAKE_IMAGE_CAMERA
        )
    }

    override fun updatePhoto(uri: Uri) {
        photo_image_view.setImageURI(uri)
    }

    override fun showDatePickerDialog() {
        val datePicker = DatePickerFragment()
        datePicker.show(supportFragmentManager, "datePicker")
        datePicker.onDateSetListener = { date ->
            if (date.isValid()) {
                date_layout.error = null
                val formattedDate = getDateFormat(date)
                date_edit_text.setText(formattedDate)
            } else {
                date_edit_text.text = null
                date_layout.error = getString(R.string.report__error_invalid_date)
            }
        }
    }

    private fun initPresenter() {
        presenter = ReportPresenter()
        presenter.view = this
        presenter.context = this
    }
}

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    var onDateSetListener: ((Date) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        onDateSetListener?.invoke(GregorianCalendar(year, month, day).time)
    }
}