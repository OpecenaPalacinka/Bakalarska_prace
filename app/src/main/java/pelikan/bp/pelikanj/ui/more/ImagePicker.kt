package pelikan.bp.pelikanj.ui.more

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import pelikan.bp.pelikanj.R
import java.io.File
import java.io.FileNotFoundException


object ImagePicker {

    private const val TEMP_IMAGE_NAME = "tempImage"

    private const val minWidthQuality = 400;

    fun getPickImageIntent(context: Context): Intent {
        lateinit var chooserIntent: Intent
        val intentList: MutableList<Intent> = ArrayList()
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoIntent.putExtra("return-data", true)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)))
        addIntentsToList(context, intentList, pickIntent)
        addIntentsToList(context, intentList, takePhotoIntent)
        if (intentList.size > 0) {
            chooserIntent = Intent.createChooser(
                intentList.removeAt(intentList.size - 1),
                context.getString(R.string.image_with_exhibit_info_label)
            )
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                intentList.toTypedArray()
            )
        }
        return chooserIntent
    }

    private fun addIntentsToList(context: Context, list: MutableList<Intent>, intent: Intent) {
        val resInfo = context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resInfo) {
            val packageName = resolveInfo.activityInfo.packageName
            val targetedIntent = Intent(intent)
            targetedIntent.setPackage(packageName)
            list.add(targetedIntent)
        }
    }

    fun getImageFromResult(context: Context, resultCode: Int, imageReturnedIntent: Intent?): Uri? {
        var selectedImage: Uri? = null
        val imageFile = getTempFile(context)
        if (resultCode == Activity.RESULT_OK) {
            val isCamera = imageReturnedIntent == null || imageReturnedIntent.data == null ||
                    imageReturnedIntent.data.toString().contains(imageFile.toString())
            selectedImage = if (isCamera) {     // CAMERA
                Uri.fromFile(imageFile)
            } else {            // ALBUM
                imageReturnedIntent!!.data
            }
        }
        return selectedImage
    }

    private fun getTempFile(context: Context): File {
        val imageFile = File(context.externalCacheDir, TEMP_IMAGE_NAME)
        if (imageFile.exists()){
            imageFile.createNewFile()
        }

        if (imageFile.parentFile is File){
            imageFile.parentFile?.mkdirs()
        }
        return imageFile
    }

    fun getBitmapFromResult(
        context: Context, resultCode: Int,
        imageReturnedIntent: Intent?
    ): Bitmap? {

        var bm: Bitmap? = null
        val imageFile: File = getTempFile(context)
        if (resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri?
            val isCamera = imageReturnedIntent == null || imageReturnedIntent.data == null ||
                    imageReturnedIntent.data.toString().contains(imageFile.toString())
            selectedImage = if (isCamera) {
                /** CAMERA  */
                Uri.fromFile(imageFile)
            } else {
                /** ALBUM  */
                imageReturnedIntent!!.data
            }

            bm = getImageResized(context, selectedImage)
            val rotation = getRotation(context, selectedImage, isCamera)
            bm = rotate(bm, rotation)
        }
        return bm
    }


    private fun decodeBitmap(context: Context, theUri: Uri?, sampleSize: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inSampleSize = sampleSize
        var fileDescriptor: AssetFileDescriptor? = null
        try {
            fileDescriptor = context.contentResolver.openAssetFileDescriptor(theUri!!, "r")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
            fileDescriptor!!.fileDescriptor, null, options
        )
        return actuallyUsableBitmap
    }

    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     */
    private fun getImageResized(context: Context, selectedImage: Uri?): Bitmap {
        lateinit var bm: Bitmap
        val sampleSizes = intArrayOf(5, 3, 2, 1)
        var i = 0
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i])
            i++
        } while (bm!!.width < minWidthQuality && i < sampleSizes.size)
        return bm
    }


    private fun getRotation(context: Context, imageUri: Uri?, isCamera: Boolean): Int {
        val rotation: Int
        rotation = if (isCamera) {
            getRotationFromCamera(context, imageUri)
        } else {
            getRotationFromGallery(context, imageUri)
        }
        return rotation
    }

    private fun getRotationFromCamera(context: Context, imageFile: Uri?): Int {
        var rotate = 0
        try {
            context.contentResolver.notifyChange(imageFile!!, null)
            val exif = ExifInterface(imageFile.path!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rotate
    }

    fun getRotationFromGallery(context: Context, imageUri: Uri?): Int {
        var result = 0
        val columns = arrayOf(MediaStore.Images.Media.ORIENTATION)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(imageUri!!, columns, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val orientationColumnIndex: Int = cursor.getColumnIndex(columns[0])
                result = cursor.getInt(orientationColumnIndex)
            }
        } catch (e: Exception) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        } //End of try-catch block
        return result
    }

    private fun rotate(bm: Bitmap, rotation: Int): Bitmap? {
        if (rotation != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
        }
        return bm
    }


}