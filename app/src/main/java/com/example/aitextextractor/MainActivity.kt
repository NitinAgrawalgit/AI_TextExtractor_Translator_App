package com.example.aitextextractor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.FileDescriptor

class MainActivity : AppCompatActivity() {

    private lateinit var resultTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val camera = findViewById<ImageView>(R.id.cameraBtn)
        val erase = findViewById<ImageView>(R.id.eraseBtn)
        val copy = findViewById<ImageView>(R.id.copyBtn)
        resultTV = findViewById(R.id.resultTV)

//        camera.setOnClickListener { v ->
//            var animation = AlphaAnimation(1.0f, 0.4f)
//            animation.duration = 130
//
//            v?.startAnimation(animation)
//
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//            if(intent.resolveActivity(packageManager) != null){
//                startActivityForResult(intent, 123)
//            } else {
//                // Something went wrong
//                Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT).show()
//            }
//        }

        camera.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

            if(intent.resolveActivity(packageManager) != null){
                startActivityForResult(intent, 100)
            }else {
                Toast.makeText(this, "Unable to Access gallery!", Toast.LENGTH_SHORT).show()
            }
        }

        erase.setOnClickListener { v ->
            var animation = AlphaAnimation(1.0f, 0.4f)
            animation.duration = 130

            v?.startAnimation(animation)

            resultTV.text = ""

            Toast.makeText(applicationContext, "Text Cleared", Toast.LENGTH_SHORT).show()
        }

        copy.setOnClickListener { v ->
            var animation = AlphaAnimation(1.0f, 0.4f)
            animation.duration = 130

            v?.startAnimation(animation)

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("text", resultTV.text.toString())

            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if(requestCode == 123 && resultCode == RESULT_OK){
//            val extras = data?.extras
//            val bitmap = extras?.get("data") as Bitmap
//            detectTextUsingML(bitmap)
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == RESULT_OK){
//            val extras = data?.extras
//            val bitmap = extras?.get("data") as Bitmap
            var imageUri = data?.data
            val parcelFileDescriptor = imageUri?.let { contentResolver.openFileDescriptor(it, "r") }
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()

            detectTextUsingML(image)
        }
    }



    private fun detectTextUsingML(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val image = InputImage.fromBitmap(bitmap, 0)

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                resultTV.text = visionText.text.toString()
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Toast.makeText(this, "Error! Text Extraction Failed", Toast.LENGTH_SHORT).show()
            }
    }
}