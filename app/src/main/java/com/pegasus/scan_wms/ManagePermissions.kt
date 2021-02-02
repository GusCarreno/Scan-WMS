package com.pegasus.scan_wms

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pegasus.scan_wms.Utils

class ManagePermissions(private val activity: Activity, private val camera:Boolean, private val gallery: Boolean, private val code:Int) {

    private val utils: Utils = Utils(activity)
    private val cameraListPermissions = listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val galleryLisPermissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    fun checkPermissions():Boolean {
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert()
            return false
        }
        return true
    }

    // Check permissions status
    private fun isPermissionsGranted(): Int {
        // PERMISSION_GRANTED : Constant Value: 0
        // PERMISSION_DENIED : Constant Value: -1
        var counter = 0
        if(camera){
            for (permission in cameraListPermissions) { counter += ContextCompat.checkSelfPermission(activity, permission) }
        }else if(gallery){
            for (permission in galleryLisPermissions) { counter += ContextCompat.checkSelfPermission(activity, permission) }
        }
        return counter
    }

    // Find the first denied permission
    private fun deniedPermission(): String {
        if(camera)
            for (permission in cameraListPermissions) { if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) return permission }
        else if(gallery)
            for (permission in galleryLisPermissions) { if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) return permission }
        return ""
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Need permission(s)")
        builder.setMessage("Some permissions are required to do the task.")
        builder.setPositiveButton("OK") { _, _ -> requestPermissions() }
        builder.setNeutralButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Show an explanation asynchronously
            if(camera)
                ActivityCompat.requestPermissions(activity, cameraListPermissions.toTypedArray(), code)
            else if(gallery)
                ActivityCompat.requestPermissions(activity, galleryLisPermissions.toTypedArray(), code)
        } else {
            utils.showShortToast("Permission can not be enable, enable the permission manually from app permissions")
        }
    }

    // Process permissions result
    fun processPermissionsResult(grantResults: IntArray): Boolean {
        var result = 0
        if (grantResults.isNotEmpty()) {
            for (item in grantResults) { result += item }
        }
        if (result == PackageManager.PERMISSION_GRANTED) return true
        return false
    }
}