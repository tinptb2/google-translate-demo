package com.example.tinpham.googletranslate

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.speech.RecognizerIntent
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val REQ_CODE_SPEECH_INPUT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TranslationUtil.inputText = input
        TranslationUtil.displayText = display

        Handler().postDelayed({
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            TranslationUtil.translateService = TranslateOptions.getDefaultInstance().service
            //TranslationUtil.translateService = TranslateOptions.newBuilder().setApiKey("InputTestAPIKey").build().service;
        },50)

        speech.setOnClickListener{
            view->
            promptSpeechInput();
        }
        input.addTextChangedListener(object : TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Handler().postDelayed({
                    TranslateTask().execute(TranslationUtil.inputText.text.toString())
                },50)
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })

    }

    object TranslationUtil {
        lateinit var inputText: EditText
        lateinit var displayText: TextView
        lateinit var translateService: Translate

        fun display(result: String){
            displayText.text = result;
        }

        fun translate(text: String?): String? {
            val translation = translateService.translate(text,
                Translate.TranslateOption.sourceLanguage("en"),
                Translate.TranslateOption.targetLanguage("vi")
            )
            Log.d("TranslateTest","Original: " + text + " Translation: " + translation.translatedText);
            return translation.translatedText
        }
    }

    class TranslateTask(): AsyncTask<String,Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            val result = TranslationUtil.translate(params[0])
            return result;
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            TranslationUtil.display(result.toString());
        }
    }



    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            getString(R.string.speech_prompt)
        )
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                applicationContext,
                getString(R.string.speech_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    TranslationUtil.inputText.setText(result[0])
                    //TranslateTask().execute(result[0])
                }
            }
        }
    }

}

