package pe.edu.esan.appostgrado.view.mas.laboratorios.ignore

import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.http.SslError
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.*
import kotlinx.android.synthetic.main.activity_tyc_pregrado_labs.*
import pe.edu.esan.appostgrado.R

class TYCPregradoLabsActivity : AppCompatActivity() {

    private var pageFinished = false

    private val LOG = TYCPregradoLabsActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tyc_pregrado_labs)

        setSupportActionBar(my_toolbar_terminos_y_condiciones)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.terminos_y_condiciones)

        my_toolbar_terminos_y_condiciones.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_terminos_y_condiciones.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        pageFinished = false
        progress_bar_tyc.visibility = View.VISIBLE
        webview_tyc.visibility = View.GONE

        if(intent.hasExtra("url_tyc")) {
            val urlTYC = intent.getStringExtra("url_tyc") ?: ""
            webview_tyc.clearCache(true)
            webview_tyc.clearFormData()
            webview_tyc.clearHistory()
            webview_tyc.clearMatches()
            webview_tyc.clearSslPreferences()

            showTerminosYCondiciones(urlTYC)

            /*showTYCAlternative(urlTYC)*/

            /*showChromeTabs(urlTYC)*/
        }
    }

    /*fun showChromeTabs(urlTYC: String){

        val doc = "<iframe src='http://docs.google.com/viewer?url=$urlTYC&embedded=true' width='100%' height='100%'  style='border: none;'></iframe>"

        webview_tyc.visibility = WebView.VISIBLE
        webview_tyc.settings.javaScriptEnabled = true
        webview_tyc.settings.allowFileAccess = true
        webview_tyc.settings.pluginState = WebSettings.PluginState.ON

        webview_tyc.webViewClient = WebViewClient()
        webview_tyc.loadData(doc, "text/html", "UTF-8")

        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()

        builder.setToolbarColor(ContextCompat.getColor(this, R.color.esan_red))
        builder.setShowTitle(true)
        val customTabsIntent: CustomTabsIntent = builder.build()

        customTabsIntent.launchUrl(this, Uri.parse("https://www.google.com"))
    }*/


    fun showTerminosYCondiciones(urlTYC: String){

        /*val url = "http://docs.google.com/gview?embedded=true&url=$urlTYC"*/

        /*val url = "https://docs.google.com/viewer?embedded=true&url=$urlTYC"*/

        val url = "https://drive.google.com/viewerng/viewer?embedded=true&url=$urlTYC"

        webview_tyc.requestFocus()
        webview_tyc.settings.javaScriptEnabled = true

        webview_tyc.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (!pageFinished) {
                    //webview_tyc.loadUrl(url)
                }
                return true
            }


            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !pageFinished) {
                    //webview_tyc.loadUrl(request!!.url.toString())
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                pageFinished = true
                webview_tyc.visibility = View.VISIBLE
                object : CountDownTimer(1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {

                    }

                    override fun onFinish() {
                        progress_bar_tyc.visibility = View.GONE
                    }
                }.start()
                super.onPageFinished(view, url)
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {

                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {

                    super.onReceivedError(view, request, error)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {

                super.onReceivedSslError(view, handler, error)
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {

                return super.shouldInterceptRequest(view, request)
            }

            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {

                return super.onRenderProcessGone(view, detail)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                super.onPageStarted(view, url, favicon)
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {

                super.onPageCommitVisible(view, url)
            }

            override fun onLoadResource(view: WebView?, url: String?) {

                super.onLoadResource(view, url)
            }

        }

        /*webview_tyc.webChromeClient = object: WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {

                super.onProgressChanged(view, newProgress)
            }
        }*/

        webview_tyc.settings.builtInZoomControls = true
        webview_tyc.settings.setSupportZoom(true)
        webview_tyc.loadUrl(url)

    }

    /*fun showTYCAlternative(pdfUrl: String){

        webview_tyc.settings.javaScriptEnabled = true
        webview_tyc.webViewClient = WebViewClient()

        webview_tyc.settings.builtInZoomControls = true
        webview_tyc.settings.setSupportZoom(true)
        webview_tyc.loadUrl(pdfUrl)

        webview_tyc.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val intent = Intent(Intent.ACTION_QUICK_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        })

    }*/



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //Respond to the action bar's Up/Home button
                //onBackPressed()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
