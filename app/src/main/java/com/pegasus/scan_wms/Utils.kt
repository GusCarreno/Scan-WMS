package com.pegasus.scan_wms

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.LocaleList
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.pegasus.scan_wms.R
import okhttp3.OkHttpClient
import java.text.Normalizer
import java.util.*
import java.util.concurrent.TimeUnit

class Utils(private val context: Context) {

    private var snackBar: Snackbar? = null

    fun showCustomLoader(llProgressBar: View) {
        llProgressBar.visibility = View.VISIBLE
        llProgressBar.bringToFront()
    }

    fun showShortToast(message:String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showLongToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun getPhoneLanguage(): String {
        return Locale.getDefault().language
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Suppress("DEPRECATION")
    fun setApplicationLanguage(language:String, country:String){
        val locale = Locale(language, country)
        // here we update locale for date formatter
        Locale.setDefault(locale)
        // here we update locale for app resources
        val res: Resources = context.resources
        val config: Configuration = res.configuration
        config.setLocales(LocaleList(locale))
        res.updateConfiguration(config, res.displayMetrics)
    }

    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS).build()
    }

    fun inputFilters(maxLenght: Int): Array<InputFilter> {
        return arrayOf<InputFilter>(LengthFilter(maxLenght), InputFilter { source, start, end, _, _, _ ->
            for (index in start until end) {
                val type = Character.getType(source[index])
                if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                    return@InputFilter ""
                }
            }
            null
        })
    }


    fun showSnakBar(view:View, message: String) {
        snackBar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction("Action", null)
        val snackView = snackBar!!.view
        snackView.setBackgroundColor(Color.RED)
        val textView = snackView.findViewById(R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        textView.gravity = Gravity.CENTER_HORIZONTAL
        snackBar!!.show()
    }

    fun hideSnackBar() {
        if(snackBar != null) snackBar!!.dismiss()
        snackBar = null
    }



    fun replaceNonAsciiCharacters(text:String): String {
        return Normalizer.normalize(text.replace("\n", " "), Normalizer.Form.NFD).replace(Regex("[^\\x00-\\x7F]"), "")
    }



}