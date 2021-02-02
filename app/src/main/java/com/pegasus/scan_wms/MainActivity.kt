package com.pegasus.scan_wms

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.github.loadingview.LoadingDialog
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.pegasus.scan_wms.MainActivity.C.Companion.PHOTOURI
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import www.sanju.motiontoast.MotionToast

class MainActivity : AppCompatActivity(), OnClickListener {


    private var canGoBack:Boolean = true
    private var hasInternet:Boolean = true
    private var saved:Boolean = false
    private lateinit var managePermissions: ManagePermissions
    private val cameraPermissionRequestCode:Int = 1
    private val galleryPermissionRequestCode:Int = 2
    private val cameraPickCode:Int = 1
    private val galleryPickCode:Int = 2
    private var utils: Utils = Utils(this)
    private lateinit var request: OkHttpRequestUtils
    private var photoBitmap: Bitmap? = null
    private var client = utils.okHttpClient()
    private val mediaTypeUrlEncoded: MediaType? =
            "application/x-www-form-urlencoded".toMediaTypeOrNull()
    private var okHttpClient: OkHttpClient? = null
    init {
        okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
    }

    lateinit var btnBarcode: Button
    lateinit var takePhoto: Button
    lateinit var btnsend: Button
    lateinit var textView: TextView
    lateinit var imageView: ImageView
    lateinit var MainView: View


    var codigo = ""
    private var imageData: ByteArray? = null
    private val postURL: String = Constant.URL + "InsertItemImage"
    companion object {
        private const val IMAGE_PICK_CODE = 999
    }

    private lateinit var mCurrentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


       // dialog
        title = "WMS-SCAN"
        btnBarcode = findViewById(R.id.button)
        textView = findViewById(R.id.txtContent)
        MainView = findViewById(R.id.relativeLayout)
        imageView = findViewById(R.id.image_view)
        takePhoto = findViewById(R.id.capture_btn)
        btnsend = findViewById(R.id.send_btn)
        textView.isVisible = false
        takePhoto.isVisible = false
        imageView.isVisible = false
        btnsend.isVisible = false

        btnBarcode.setOnClickListener {
            val intentIntegrator = IntentIntegrator(this@MainActivity)
            intentIntegrator.setBeepEnabled(false)
            intentIntegrator.setCameraId(0)
            intentIntegrator.setPrompt("SCAN")
            intentIntegrator.setBarcodeImageEnabled(false)
            intentIntegrator.initiateScan()
        }
        takePhoto.setOnClickListener(this)
        btnsend.setOnClickListener {

            createMenu(codigo)
        }

    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.capture_btn -> ItemItemBottomSheet(this).show(supportFragmentManager, "ItemItemBottomSheet")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                val dialog = LoadingDialog.get(this).show()
                if (result.contents == null) {
                    dialog.hide()
                    Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    //Log.d("MainActivity", "Scanned")
                    Toast.makeText(this, "Barcode -> " + result.contents, Toast.LENGTH_LONG)
                            .show()
                    codigo = result.contents
                    //textView.text = String.format("Scanned Result: %s", result)
                    infoProducto(url = Constant.URL + "ReturnItemInformation", Barcode = codigo, onSuccess = { result ->

                        var str_response = result.toString()

                        val gson = Gson()
                        val homedateList: List<Item> = gson.fromJson(str_response, Array<Item>::class.java).toList()
                        val info = "Item Id" + homedateList[0].ItemId + " , Item Nombre" +homedateList[0].ItemNombre


                        // textView.isVisible = true
                        runOnUiThread {
                            try {
                                //dialog?.hide()
                                textView.text = info
                                textView.isVisible = false
                                takePhoto.isVisible = true

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    },
                            onError = { result ->
                                //Log.e(tag, "$result")
                                //presenter?.onLoadTwitsFail()
                                runOnUiThread {
                                    takePhoto.isVisible = true
                                    dialog?.hide()
                                    MotionToast.createToast(
                                            this,
                                            "Barcode-WMS",
                                            "Producto no encontrado , por favor toma una foto y envianos la informacion",
                                            MotionToast.TOAST_WARNING,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(this, R.font.montserrat_regular))

                                }
                            })
                }

            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
            if (requestCode == cameraPermissionRequestCode && resultCode == Activity.RESULT_OK) { // CAMERA RESULT
                photoBitmap = ImagesUtils().getBitmapFromUri(this, PHOTOURI!!)
                imageView.setImageURI(PHOTOURI)
                btnsend.isVisible = true
                PHOTOURI = null
            }else if (requestCode == galleryPermissionRequestCode && resultCode == Activity.RESULT_OK && data != null && data.data != null) { // GALLERY RESULT
                photoBitmap = ImagesUtils().getBitmapFromGallery(this, data)
                imageView.setImageBitmap(photoBitmap)
            }
            imageView.visibility = VISIBLE
        }catch (e: Exception) {
            utils.showShortToast(getString(R.string.system_error_processing_data))
        }
    }


    fun checkPermissions(context: Context, permissionCode: Int) {
        managePermissions = if(permissionCode == cameraPermissionRequestCode) ManagePermissions(context as Activity, camera = true, gallery = false, code = cameraPermissionRequestCode)
        else  ManagePermissions(context as Activity, camera = false, gallery = true, code = cameraPermissionRequestCode)
        val res = managePermissions.checkPermissions()
        if(res && permissionCode == cameraPermissionRequestCode) openCamera(context)
        else if(res && permissionCode == galleryPermissionRequestCode) openGallery(context)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            cameraPermissionRequestCode -> {
                managePermissions = ManagePermissions(this, camera = true, gallery = false, code = cameraPermissionRequestCode)
                val isPermissionsGranted = managePermissions.processPermissionsResult(
                        grantResults
                )
                if (isPermissionsGranted) openCamera(this)
                return
            }
            galleryPermissionRequestCode -> {
                managePermissions = ManagePermissions(this, camera = false, gallery = true, code = cameraPermissionRequestCode)
                val isPermissionsGranted = managePermissions.processPermissionsResult(
                        grantResults
                )
                if (isPermissionsGranted) openGallery(this)
                return
            }
        }
    }

    private fun openCamera(activity: Activity) {
        try {
            utils = Utils(activity)
            PHOTOURI = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, PHOTOURI)
            if (cameraIntent.resolveActivity(activity.packageManager) != null)
                activity.startActivityForResult(cameraIntent, cameraPickCode)
        }catch (e: Exception){ utils.showLongToast(activity.getString(R.string.system_error_open_camera)) }
    }

    private fun openGallery(activity: Activity){
        try {
            utils = Utils(activity)
            val galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            if (galleryIntent.resolveActivity(activity.packageManager) != null)
                activity.startActivityForResult(Intent.createChooser(galleryIntent, "SELECT AN IMAGE"), galleryPickCode)
        }catch (e: Exception){ utils.showLongToast(activity.getString(R.string.system_error_open_camera)) }
    }



    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun createMenu(barcode: String) {
        val dialog = LoadingDialog.get(this).show()
        request = OkHttpRequestUtils(this, client)
        val map: HashMap<String, String> = hashMapOf(
                "Barcode" to utils.replaceNonAsciiCharacters(barcode))

        request.postImage(postURL, photoBitmap, "producto-" + barcode + ".png", map, object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    utils.showLongToast(getString(R.string.system_error_processing_data))

                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        println("response" + response)

                        btnsend.isVisible = false
                        takePhoto.isVisible = false
                        textView.isVisible = false
                        imageView.setImageResource(0)
                        imageView.isVisible = false
                        exito()
                        //finish()
                        dialog.hide()
                    } catch (e: Exception) {
                        dialog.hide()
                        intento()

                        utils.showLongToast(getString(R.string.system_error_processing_data))
                    }
                }
            }
        })
    }

    fun intento(){
        MotionToast.createToast(
                this,
                "Barcode - WMS",
                "Favor intente mandar la informacion de nuevo",
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, R.font.montserrat_regular))
    }

    fun exito(){
        MotionToast.createToast(
                this,
                "Barcode - WMS",
                "Producto registrado correctamente",
                MotionToast.TOAST_SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, R.font.montserrat_regular))
    }

    fun infoProducto(
            url: String,
            Barcode: String,
            onSuccess: ((result: Any?) -> Unit)?,
            onError: ((result: Any?) -> Unit)?
    ) {
        var params = "&Barcode="+ Barcode + "&CustomerId=HUGOMARKET&key=WMS_v2"
        println("PARAMETROS" + params)
        val body = params.toRequestBody(contentType = mediaTypeUrlEncoded)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .tag(url)
                .build()

        okHttpClient
                ?.newCall(request)
                ?.enqueue(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        //LoginActivity?.runOnUiThread {
                        onError?.invoke(e.message)
                        //}
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val xmlResponse = response.body?.string()

                        // LocalizarActivity?.runOnUiThread {
                        if (response.code == 200)
                            onSuccess?.invoke(xmlResponse)
                        else
                            onError?.invoke("Lo sentimos no hay datos disponibles")
                        //}
                    }
                })
    }
    class C {
        companion object {
            var PHOTOURI: Uri? = null
        }
    }

}
