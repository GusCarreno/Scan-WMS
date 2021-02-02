package com.pegasus.scan_wms

import android.content.Context
import android.graphics.Bitmap
import com.pegasus.scan_wms.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class OkHttpRequestUtils(context: Context, client: OkHttpClient) {
    private var client = OkHttpClient()
    private var key = ""
    private var apiBaseUrl = ""

    init {
        this.client = client
        this.key = "WMS_v2"
    }

    fun post(url: String, parameters: HashMap<String, String>, callback: Callback): Call {
        val builder = FormBody.Builder()
        val it = parameters.entries.iterator()
        while (it.hasNext()) {
            val pair = it.next() as Map.Entry<*, *>
            builder.add(pair.key.toString(), pair.value.toString())
        }
        builder.add("key", key)

        val formBody = builder.build()
        val request = Request.Builder()
            .url( url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(formBody)
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun postImage(url: String, image: Bitmap?, fileName:String, parameters: HashMap<String, String>?, callback: Callback): Call {

        val request = Request.Builder()
            .url(url)
            .header("key" ,key)
            .header("Content-Type", if(image == null) "application/x-www-form-urlencoded" else "multipart/form-data")
            .header("Content-Disposition", "form-data")
        if(image != null) {
            val requestBody: MultipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file",fileName, ImagesUtils().getBitmapArray(image,80).toRequestBody("image/png".toMediaTypeOrNull(), 0))
                .build()
            request.post(requestBody)
        }
        if(parameters != null ){
            val it = parameters.entries.iterator()
            while (it.hasNext()) {
                val pair = it.next() as Map.Entry<*, *>
                request.header(pair.key.toString(), pair.value.toString())
            }
        }
        val call = client.newCall(request.build())
        call.enqueue(callback)
        return call
    }

    fun get(url: String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

}
