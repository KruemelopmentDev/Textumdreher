package de.kruemelopment.org.textumdreher_reherdmutxet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class Popup : AppCompatActivity() {
    private var clipboard: ClipboardManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var text = intent.getStringExtra("text")
        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (text == null) {
            text = if (clipboard!!.primaryClip != null) {
                clipboard!!.primaryClip!!.getItemAt(0).text.toString()
            } else ""
        }
        setContentView(R.layout.popuplayout)
        val editText = findViewById<TextInputEditText>(R.id.editText2)
        val editText2 = findViewById<TextInputEditText>(R.id.editText3)
        val speichern = findViewById<FloatingActionButton>(R.id.button6)
        val teilen = findViewById<FloatingActionButton>(R.id.button5)
        editText.setText(text)
        editText2.setText(StringBuffer(text).reverse().toString())
        speichern.setOnClickListener {
            if (clipboard != null) {
                if (editText2.text == null) return@setOnClickListener
                clipboard!!.setPrimaryClip(
                    ClipData.newPlainText(
                        "Textumdreher",
                        editText2.text.toString()
                    )
                )
                MyToast(this).showSuccess("Text in die Zwischenablage kopiert.")
                finishAndRemoveTask()
            } else {
                MyToast(this).showError("Fehler")
            }
        }
        teilen.setOnClickListener {
            if (editText2.text == null) return@setOnClickListener
            val sendIntent = Intent()
            sendIntent.setAction(Intent.ACTION_SEND)
            sendIntent.putExtra(Intent.EXTRA_TEXT, editText2.text.toString())
            sendIntent.setType("text/plain")
            startActivity(sendIntent)
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editText2.setText(StringBuffer(s).reverse().toString())
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }
}
