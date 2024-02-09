package com.example.aitextextractor

import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

class Utils {
    companion object {
        fun getAllLanguages(): ArrayList<String>{
            val languageCodes = TranslateLanguage.getAllLanguages()

            val languages = ArrayList<String>()

            for(code in languageCodes){
                languages.add(Locale(code).displayName)
            }

            return languages
        }

        fun getPositionFromLanguageCode(code: String): Int {
            val languageCodes = TranslateLanguage.getAllLanguages()
            return languageCodes.indexOf(code)
        }

        fun getLanguageCodeFromPosition(position: Int): String {
            val languageCodes = TranslateLanguage.getAllLanguages()
            return languageCodes[position]
        }
    }
}