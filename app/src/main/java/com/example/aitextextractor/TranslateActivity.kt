package com.example.aitextextractor

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Selection
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslateActivity : AppCompatActivity() {

    private lateinit var originalTextET: EditText
    private lateinit var translatedTextET: EditText

    private var isEditModeON = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        originalTextET = findViewById(R.id.original_text)
        translatedTextET = findViewById(R.id.translated_text)

        var backBtn = findViewById<LinearLayout>(R.id.back_btn)
        var copyOriginal = findViewById<ImageView>(R.id.copyBtnOriginal)
        var copyTranslated = findViewById<ImageView>(R.id.copyBtnTranslated)
        var editBtn = findViewById<ImageView>(R.id.editBtn)

        val text = intent.getStringExtra("text")
        originalTextET.setText(text)

        if (text != null) {
            prepareTranslator(text)
        }

        backBtn.setOnClickListener { v ->
            val animation = AnimationUtils.loadAnimation(this, R.anim.bounce_effect)
            v.startAnimation(animation)

            finish()
            //Remember to close the translator object here. TO BE IMPLEMENTED LATER
        }

        copyOriginal.setOnClickListener { v ->
            val animation = AnimationUtils.loadAnimation(this, R.anim.bounce_effect)
            v.startAnimation(animation)
//
//            val colorFrom = Color.DKGRAY
//            val colorTo = ContextCompat.getColor(this, R.color.gradient_start)
//            var animator = ObjectAnimator.ofObject(v, "backgroundColor", ArgbEvaluator(), colorFrom, colorTo)
//            animator.duration = 150
//            animator.start()


            val textString = originalTextET.text.toString()

            if(textString.isEmpty()){
                Toast.makeText(this, "Error! TextBox is Empty", Toast.LENGTH_SHORT).show()
            }else {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("text", textString)

                clipboard.setPrimaryClip(clip)

                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        copyTranslated.setOnClickListener { v ->
            val animation = AnimationUtils.loadAnimation(this, R.anim.bounce_effect)
            v.startAnimation(animation)

            val textString = translatedTextET.text.toString()

            if(textString.isEmpty()){
                Toast.makeText(this, "Error! TextBox is Empty", Toast.LENGTH_SHORT).show()
            }else {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("text", textString)

                clipboard.setPrimaryClip(clip)

                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        editBtn.setOnClickListener {
            if(isEditModeON){
                editBtn.setImageResource(R.drawable.edit_icon)
                originalTextET.isEnabled = false

                isEditModeON = false
            }else {
                editBtn.setImageResource(R.drawable.check_icon)
                originalTextET.isEnabled = true

                val length = originalTextET.text.toString().length
                originalTextET.setSelection(length)
                originalTextET.requestFocus()
                openKeyboard()

                isEditModeON = true
            }
        }
    }

    private fun prepareTranslator(text: String){
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.HINDI)
            .build()

        val englishHindiTranslator = Translation.getClient(options)

        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        englishHindiTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                // (Set a flag, unhide the translation UI, etc.)
                Toast.makeText(this, "Language Model Downloaded Successfully!", Toast.LENGTH_SHORT).show()
                translateText(englishHindiTranslator, text)
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
                // ...
                Toast.makeText(this, "Language Model Download Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun translateText(translator: Translator, text: String){
        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                // Translation successful.
                translatedTextET.setText(translatedText)
            }
            .addOnFailureListener { exception ->
                // Error.
                // ...
                Toast.makeText(this, "Error! Text Translation Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openKeyboard(){
        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}