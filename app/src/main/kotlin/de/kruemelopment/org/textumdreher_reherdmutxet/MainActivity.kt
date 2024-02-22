package de.kruemelopment.org.textumdreher_reherdmutxet

import android.Manifest
import android.app.ActivityManager
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var text: EditText? = null
    private var popup: MenuItem? = null
    private var nightmode = false
    private var clipboard: ClipboardManager? = null
    private var myToast: MyToast? = null
    private var someActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val sp2 = getSharedPreferences("settings", 0)
            nightmode = sp2.getBoolean("mode", false)
            if (nightmode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myToast = MyToast(this)
        val sese = getSharedPreferences("Start", 0)
        val web = sese.getBoolean("agbs", false)
        if (!web) {
            val dialog = Dialog(this, R.style.Dialog)
            dialog.setContentView(R.layout.webdialog)
            val ja = dialog.findViewById<TextView>(R.id.textView5)
            val nein = dialog.findViewById<TextView>(R.id.textView8)
            ja.setOnClickListener {
                val ed = sese.edit()
                ed.putBoolean("agbs", true)
                ed.apply()
                val dip = 80f
                val r = resources
                val px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dip,
                    r.displayMetrics
                )
                val sp = getSharedPreferences("Einstellungen", 0)
                val ede = sp.edit()
                ede.putInt("hoehe", px.toInt())
                ede.apply()
                dialog.dismiss()
                requestPermission()
            }
            nein.setOnClickListener { finishAndRemoveTask() }
            val textView = dialog.findViewById<TextView>(R.id.textView4)
            textView.text = Html.fromHtml(
                "Mit der Nutzung dieser App aktzeptiere ich die " +
                        "<a href=\"https://www.kruemelopment-dev.de/datenschutzerklaerung\">Datenschutzerklärung</a>" + " und die " + "<a href=\"https://www.kruemelopment-dev.de/nutzungsbedingungen\">Nutzungsbedingungen</a>" + " von Krümelopment Dev",HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            textView.movementMethod = LinkMovementMethod.getInstance()
            dialog.setCancelable(false)
            dialog.show()
        } else requestPermission()
        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        text = findViewById(R.id.editText)
        val tv = findViewById<TextView>(R.id.textView)
        tv.movementMethod = ScrollingMovementMethod()
        tv.setOnLongClickListener {
            clipboard!!.setPrimaryClip(ClipData.newPlainText("Textumdreher", tv.text.toString()))
            myToast!!.showSuccess("Text in die Zwischenablage kopiert.")
            true
        }
        val shareButton = findViewById<FloatingActionButton>(R.id.share)
        shareButton.setOnClickListener {
            val input = text!!.text.toString()
            if (check(input)) {
                share(StringBuffer(input).reverse().toString())
            }
        }
        val reverseButton = findViewById<FloatingActionButton>(R.id.umdrehen)
        reverseButton.setOnClickListener {
            val input = text!!.text.toString()
            if (check(input)) {
                tv.text = StringBuffer(input).reverse().toString()
            }
        }
        val deleteButton = findViewById<FloatingActionButton>(R.id.delete)
        deleteButton.setOnClickListener {
            text!!.setText("")
            myToast!!.showSuccess("Alles gelöscht")
        }
        deleteButton.setOnLongClickListener {
            text!!.setText("")
            tv.text = ""
            myToast!!.showSuccess("Alles gelöscht")
            false
        }
        val printButton = findViewById<FloatingActionButton>(R.id.print)
        printButton.setOnClickListener {
            if (tv.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                try {
                    val file = File(filesDir, "drucken.txt")
                    val writer = FileWriter(file)
                    writer.append(tv.text.toString())
                    writer.flush()
                    writer.close()
                    val shareIntent = Intent()
                    shareIntent.setAction(Intent.ACTION_SEND)
                    shareIntent.putExtra(
                        Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            this@MainActivity,
                            "de.kruemelopment.org.textumdreher_reherdmutxet.provider",
                            file
                        )
                    )
                    shareIntent.setType("application/txt")
                    startActivity(Intent.createChooser(shareIntent, "Drucker auswählen"))
                } catch (e: IOException) {
                    myToast!!.showError("Fehler beim Speichern!")
                }
            }
        }
        someActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this@MainActivity, CopyService::class.java))
                popup!!.setChecked(true)
            } else popup!!.setChecked(false)
        }
    }

    private fun check(text: String): Boolean {
        return text.trim { it <= ' ' }.replace("\n", "").isNotEmpty()
    }

    private fun share(text: String) {
        if (check(text)) {
            val shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(Intent.createChooser(shareIntent, "Teilen mit..."))
        } else myToast!!.showError("Lass die App erstmal einen Text umdrehen, bevor du ihn teilen willst!")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dots, menu)
        popup = menu.findItem(R.id.popuplol)
        popup!!.setChecked(isMyServiceRunning)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            popup!!.setVisible(false)
        }
        val darkmode = menu.findItem(R.id.nightmode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            darkmode.setVisible(false)
        } else {
            darkmode.setChecked(nightmode)
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun openLink(link: String) {
        val urid = Uri.parse(link)
        val intentd = Intent(Intent.ACTION_VIEW, urid)
        startActivity(intentd)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item4 -> {
                openLink("https://www.kruemelopment-dev.de/nutzungsbedingungen")
                return true
            }
            R.id.item5 -> {
                openLink("https://www.kruemelopment-dev.de/datenschutzerklärung")
                return true
            }
            R.id.item6 -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.setData(Uri.parse("mailto:kontakt@kruemelopment-dev.de"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }
            R.id.popuplol -> {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    if (!item.isChecked) {
                        val intent2 = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        someActivityResultLauncher!!.launch(intent2)
                    } else {
                        if (isMyServiceRunning) {
                            stopService(Intent(this@MainActivity, CopyService::class.java))
                        }
                    }
                    item.setChecked(false)
                } else {
                    if (!item.isChecked) {
                        if (!isMyServiceRunning) {
                            startService(Intent(this@MainActivity, CopyService::class.java))
                        }
                        item.setChecked(true)
                    } else {
                        if (isMyServiceRunning) {
                            stopService(Intent(this@MainActivity, CopyService::class.java))
                        }
                        item.setChecked(false)
                    }
                }
                return true
            }
            R.id.nightmode -> {
                nightmode = !nightmode
                val sp = getSharedPreferences("settings", 0)
                val e = sp.edit()
                e.putBoolean("mode", nightmode)
                e.apply()
                if (nightmode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private val isMyServiceRunning: Boolean
        get() {
            val manager = (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (CopyService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                14
            )
        }
    }
}