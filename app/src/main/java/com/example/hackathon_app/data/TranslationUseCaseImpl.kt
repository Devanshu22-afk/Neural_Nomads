package com.example.hackathon_app.data

import android.content.Context
import android.widget.Toast
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslationUseCaseImpl(private val context: Context) {
    suspend fun translate(text: String, fromLangTag: String, toLangTag: String): String {
        val fromLang = mapToMlKitLang(fromLangTag)
        val toLang = mapToMlKitLang(toLangTag)
        if (fromLang == null || toLang == null) {
            Toast.makeText(context, "Selected language not supported for translation.", Toast.LENGTH_LONG).show()
            return text
        }
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(fromLang)
            .setTargetLanguage(toLang)
            .build()
        val translator = Translation.getClient(options)
        try {
            translator.downloadModelIfNeeded().await()
            val translated = translator.translate(text).await()
            translator.close()
            return translated
        } catch (e: Exception) {
            Toast.makeText(context, "Translation failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            translator.close()
            return text
        }
    }

    // Dynamically map Android locale tags to ML Kit language codes
    private fun mapToMlKitLang(tag: String): String? {
        // Try to use ML Kit's built-in mapping
        return try {
            TranslateLanguage.fromLanguageTag(tag.substring(0, 2))
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        // List of all supported language tags in your app
        val supportedLanguageTags = listOf("hi-IN", "en-US", "ta-IN", "bn-IN", "gu-IN", "mr-IN")
    }

    fun preDownloadAllModels(onComplete: (() -> Unit)? = null) {
        var completed = 0
        val total = supportedLanguageTags.size * (supportedLanguageTags.size - 1)
        for (fromTag in supportedLanguageTags) {
            for (toTag in supportedLanguageTags) {
                if (fromTag == toTag) continue
                val fromLang = mapToMlKitLang(fromTag)
                val toLang = mapToMlKitLang(toTag)
                if (fromLang != null && toLang != null) {
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(fromLang)
                        .setTargetLanguage(toLang)
                        .build()
                    val translator = Translation.getClient(options)
                    translator.downloadModelIfNeeded()
                        .addOnCompleteListener {
                            completed++
                            if (completed == total) {
                                onComplete?.invoke()
                            }
                            translator.close()
                        }
                }
            }
        }
    }
} 