package com.example.aitextextractor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.FileDescriptor

class UploadImgActivity : AppCompatActivity() {

    val CAMERA_REQUEST_CODE = 101
    val GALLERY_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_img)

        var cameraBtn = findViewById<ImageView>(R.id.cameraBtn)
        var galleryBtn = findViewById<ImageView>(R.id.galleryBtn)

        cameraBtn.setOnClickListener { v ->
            var clickAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_effect)
            v.startAnimation(clickAnimation)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if(intent.resolveActivity(packageManager) != null){
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                // Something went wrong
                Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT).show()
            }
        }

        galleryBtn.setOnClickListener {v ->
            var clickAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_effect)
            v.startAnimation(clickAnimation)

            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

            if(intent.resolveActivity(packageManager) != null){
                startActivityForResult(intent, GALLERY_REQUEST_CODE)
            }else {
                Toast.makeText(this, "Unable to Access gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            val extras = data?.extras
            val bitmap = extras?.get("data") as Bitmap

            detectTextUsingML(bitmap)
        }else if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            var imageUri = data?.data //This URI can also be used to set as resource for an Imageview

            val parcelFileDescriptor = imageUri?.let { contentResolver.openFileDescriptor(it, "r") }
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val imageBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()

            detectTextUsingML(imageBitmap)
        }
    }

    private fun detectTextUsingML(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val image = InputImage.fromBitmap(bitmap, 0)

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                val s = visionText.text.toString()
//                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
                sendTextToTranslateActivity(s)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Toast.makeText(this, "Error! Text Extraction Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendTextToTranslateActivity(text: String){
        var intent = Intent(this, TranslateActivity::class.java)
        intent.putExtra("text", text)
        startActivity(intent)
    }
}