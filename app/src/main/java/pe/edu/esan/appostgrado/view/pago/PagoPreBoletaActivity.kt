package pe.edu.esan.appostgrado.view.pago

import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_pago_pre_boleta.*
import kotlinx.android.synthetic.main.toolbar_menuprincipal.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.util.Utilitarios

class PagoPreBoletaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago_pre_boleta)

        val toolbar = pagopreboleta_toolbar as Toolbar
        toolbar.toolbar_title.text = resources.getString(R.string.recibopago)

        setSupportActionBar(toolbar)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val returnArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
            returnArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            supportActionBar?.setHomeAsUpIndicator(returnArrow)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar!!.setHomeButtonEnabled(true)

        val extras = intent.extras
        if (extras != null) {

            val codigo = extras["KEY_CODBOLETA"] as String
            println(codigo)
            getCargaPDF(codigo)
        }
    }

    private fun getCargaPDF(codigo: String) {
        prbCargando_pagopreboleta.visibility = View.VISIBLE
        //pdf_pagopreboleta.fromUri(Uri.parse(Utilitarios.getBoletasPreUrl(codigo)))
        pdf_pagopreboleta.settings.javaScriptEnabled = true
        pdf_pagopreboleta.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=${Utilitarios.getBoletasPreUrl(codigo)}")

        pdf_pagopreboleta.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                prbCargando_pagopreboleta.visibility = View.GONE
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
