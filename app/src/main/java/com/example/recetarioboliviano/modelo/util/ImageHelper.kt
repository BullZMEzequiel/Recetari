package com.example.recetarioboliviano.modelo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.recetarioboliviano.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Helper para centralizar la lógica de manejo de imágenes en la aplicación.
 */
object ImageHelper {

    /**
     * Carga una imagen en un ImageView usando Glide con placeholder.
     */
    fun cargarImagen(imageView: ImageView, uriString: String?) {
        val context = imageView.context
        if (uriString.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder)
            return
        }

        val uri = try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            null
        }

        if (uri == null) {
            imageView.setImageResource(R.drawable.ic_image_placeholder)
            return
        }

        // Si es un archivo local, verificar si existe
        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (!file.exists()) {
                imageView.setImageResource(R.drawable.ic_image_placeholder)
                return
            }
        }

        Glide.with(context)
            .load(uri)
            .centerCrop()
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(imageView)
    }

    /**
     * Carga un avatar circular.
     */
    fun cargarAvatar(imageView: ImageView, uriString: String?) {
        val context = imageView.context
        if (uriString.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_avatar_default)
            return
        }

        val uri = try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            null
        }

        Glide.with(context)
            .load(uri)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_default)
            .error(R.drawable.ic_avatar_default)
            .into(imageView)
    }

    /**
     * Copia y OPTIMIZA una imagen de una URI externa a un archivo local.
     * Reduce la resolución y comprime para ahorrar espacio en disco (Especial para modo offline).
     */
    fun copiarImagenAArchivoLocal(context: Context, uri: Uri): String? {
        return try {
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            val optimizedBitmap = decodeAndResizeBitmap(context, uri, 1024) ?: return null
            
            val outputStream = FileOutputStream(file)
            optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.close()
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodifica y redimensiona un Bitmap manteniendo la relación de aspecto y corrigiendo rotación.
     */
    private fun decodeAndResizeBitmap(context: Context, uri: Uri, maxSize: Int): Bitmap? {
        var input: InputStream? = context.contentResolver.openInputStream(uri) ?: return null

        // 1. Obtener dimensiones originales
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(input, null, options)
        input?.close()

        var width = options.outWidth
        var height = options.outHeight

        // 2. Calcular factor de escala
        var inSampleSize = 1
        if (width > maxSize || height > maxSize) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= maxSize || halfWidth / inSampleSize >= maxSize) {
                inSampleSize *= 2
            }
        }

        // 3. Decodificar con sample size
        input = context.contentResolver.openInputStream(uri)
        val resampledBitmap = BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        })
        input?.close()

        if (resampledBitmap == null) return null

        // 4. Corregir rotación basada en EXIF si es necesario
        return try {
            val exifInput = context.contentResolver.openInputStream(uri)
            val exif = exifInput?.use { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            
            Bitmap.createBitmap(resampledBitmap, 0, 0, resampledBitmap.width, resampledBitmap.height, matrix, true)
        } catch (e: Exception) {
            resampledBitmap
        }
    }
}
