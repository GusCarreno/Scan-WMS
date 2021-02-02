package com.pegasus.scan_wms

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.pegasus.scan_wms.R
import java.io.ByteArrayOutputStream

class ImagesUtils {

    private fun resizeBitmap(source: Bitmap, maxHeight: Int): Bitmap {
        try {
            if (source.height >= source.width) {
                if (source.height <= maxHeight) { // if image height already smaller than the required height
                    return source
                }

                val aspectRatio = source.width.toDouble() / source.height.toDouble()
                val targetWidth = (maxHeight * aspectRatio).toInt()
                return Bitmap.createScaledBitmap(source, targetWidth, maxHeight, false)
            } else {
                if (source.width <= maxHeight) { // if image width already smaller than the required width
                    return source
                }

                val aspectRatio = source.height.toDouble() / source.width.toDouble()
                val targetHeight = (maxHeight * aspectRatio).toInt()

                return Bitmap.createScaledBitmap(source, maxHeight, targetHeight, false)
            }
        } catch (e: Exception) {
            return source
        }
    }

    fun loadImage(context: Context, url:String, imgView: ImageView, signature:Boolean = false){
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        val requestOptions = if(signature) {
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).signature(
                    ObjectKey(System.currentTimeMillis())
            )
        }else{
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        }

        Glide.with(context)
                .load(url)
                .apply(requestOptions)
                .thumbnail(Glide.with(context).load(url))
                .fallback(R.color.color_start)
                .error(R.color.color_start)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(circularProgressDrawable)
                .into(imgView)
        circularProgressDrawable.stop()
    }

    fun getBitmapArray(bitmap:Bitmap, quality:Int): ByteArray {
        var newBitmap:Bitmap = if((bitmap.byteCount / 1024) > 100) resizeBitmap(bitmap,700) else bitmap
        val stream = ByteArrayOutputStream()
        newBitmap.compress(Bitmap.CompressFormat.PNG, quality, stream)
        return stream.toByteArray()
    }

    fun getBitmapFromUri(context:Context,uri: Uri): Bitmap {
        return if(Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        else ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    }

    fun getBitmapFromGallery(context:Context, data: Intent?): Bitmap {
        return if(Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(context.contentResolver, data!!.data!!)
        else ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, data!!.data!!))
    }
}