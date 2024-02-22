package de.kruemelopment.org.textumdreher_reherdmutxet

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import java.util.Objects

class Quickdreher : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = Objects.requireNonNull(intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT))
            .toString()
        val ruckwarts = StringBuffer(action).reverse().toString()
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Textumdreher", ruckwarts)
        clipboard.setPrimaryClip(clip)
        MyToast(this).showSuccess("Umgedrehten Text in die Zwischenablage kopiert!")
        finishAndRemoveTask()
    }
}
