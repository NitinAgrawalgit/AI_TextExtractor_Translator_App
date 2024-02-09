package com.example.aitextextractor

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Color
import android.graphics.Interpolator
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Selection
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import okhttp3.internal.Util

class TranslateActivity : AppCompatActivity() {

    private lateinit var originalTextET: EditText
    private lateinit var translatedTextET: EditText
    private lateinit var sourceLangSpinner: Spinner
    private lateinit var targetLangSpinner: Spinner

    private lateinit var sourceLang: String
    private lateinit var targetLang: String

    private lateinit var translator: Translator

    private var isEditModeON = false
    private var isInitializingSource = true
    private var isInitializingTarget = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        originalTextET = findViewById(R.id.original_text)
        translatedTextET = findViewById(R.id.translated_text)
        sourceLangSpinner = findViewById<Spinner>(R.id.original_languageBtn)
        targetLangSpinner = findViewById<Spinner>(R.id.translate_languageBtn)

        val backBtn = findViewById<LinearLayout>(R.id.back_btn)
        val copyOriginal = findViewById<ImageView>(R.id.copyBtnOriginal)
        val copyTranslated = findViewById<ImageView>(R.id.copyBtnTranslated)
        val editBtn = findViewById<ImageView>(R.id.editBtn)
        val swapBtn = findViewById<ImageView>(R.id.switch_languagesBtn)

        val text = intent.getStringExtra("text")
        originalTextET.setText(text)

        initLanguages()

        setUpSourceSpinner()
        setUpTargetSpinner()

        makeModel()

        backBtn.setOnClickListener { v ->
            backPress(v)
        }

        copyOriginal.setOnClickListener { v ->
            copySource(v)
        }

        copyTranslated.setOnClickListener { v ->
            copyTranslation(v)
        }

        swapBtn.setOnClickListener { v ->
            swapLanguages(v)
        }

        editBtn.setOnClickListener {
            if(isEditModeON){
                editBtn.setImageResource(R.drawable.edit_icon)
                originalTextET.isEnabled = false

                isEditModeON = false
                translatedTextET.setText("Translating...")
                translateText()
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

    private fun initLanguages(){
        sourceLang = TranslateLanguage.ENGLISH
        targetLang = TranslateLanguage.HINDI
    }

    private fun setUpSourceSpinner(){
        val spinnerAdapter = ArrayAdapter(this, R.layout.language_spinner_item, Utils.getAllLanguages())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sourceLangSpinner.adapter = spinnerAdapter
        sourceLangSpinner.setSelection(Utils.getPositionFromLanguageCode(sourceLang))

        sourceLangSpinner.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sourceLang = Utils.getLanguageCodeFromPosition(position)
                translatedTextET.setText("Installing Language Model...")
                if(isInitializingSource){
                    isInitializingSource = false
                }else {
                    makeModel()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                sourceLang = TranslateLanguage.ENGLISH
                sourceLangSpinner.setSelection(Utils.getPositionFromLanguageCode(sourceLang))
            }
        }
    }

    private fun setUpTargetSpinner(){
        val spinnerAdapter = ArrayAdapter(this, R.layout.language_spinner_item, Utils.getAllLanguages())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        targetLangSpinner.adapter = spinnerAdapter
        targetLangSpinner.setSelection(Utils.getPositionFromLanguageCode(targetLang))

        targetLangSpinner.onItemSelectedListener = object: OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                targetLang = Utils.getLanguageCodeFromPosition(position)
                translatedTextET.setText("Installing Language Model...")
                if(isInitializingTarget){
                    isInitializingTarget = false
                }else {
                    makeModel()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                targetLang = TranslateLanguage.HINDI
                targetLangSpinner.setSelection(Utils.getPositionFromLanguageCode(targetLang))
            }
        }
    }

    private fun makeModel(){
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

        translator = Translation.getClient(options)

        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Toast.makeText(this, "Language Model Installed", Toast.LENGTH_SHORT).show()
                translatedTextET.setText("Translating...")

                translateText()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Language Model Unavailable", Toast.LENGTH_SHORT).show()
            }
    }

    private fun translateText(){
        var sourceText = originalTextET.text.toString()

        translator.translate(sourceText)
            .addOnSuccessListener {s ->
                translatedTextET.setText(s)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Translation Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openKeyboard(){
        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun copySource(v: View){
        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce_effect)
        v.startAnimation(animation)

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

    private fun copyTranslation(v: View){
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

    private fun backPress(v: View){
        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce_effect)
        v.startAnimation(animation)

        //Remember to close the translator object here. TO BE IMPLEMENTED LATER
        finish()
    }

    private fun swapLanguages(v: View){
        val animation = AnimationUtils.loadAnimation(this, R.anim.rotate_effect)
        animation.duration = 500
        v.startAnimation(animation)

        sourceLang = targetLang.also { targetLang = sourceLang }

        val sourceText = originalTextET.text.toString()
        val targetText = translatedTextET.text.toString()

        originalTextET.setText(targetText)
        translatedTextET.setText(sourceText)

        isInitializingSource = true
        isInitializingTarget = true
        sourceLangSpinner.setSelection(Utils.getPositionFromLanguageCode(sourceLang))
        targetLangSpinner.setSelection(Utils.getPositionFromLanguageCode(targetLang))

        makeModel()
    }
}