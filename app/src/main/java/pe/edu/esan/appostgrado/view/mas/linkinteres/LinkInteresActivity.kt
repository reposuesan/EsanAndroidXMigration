package pe.edu.esan.appostgrado.view.mas.linkinteres

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_link_interes.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.LinkInteresAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.Link
import pe.edu.esan.appostgrado.util.Utilitarios

class LinkInteresActivity : AppCompatActivity() {

    private val LOG = LinkInteresActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_interes)

        val toolbar = main_link as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.link_interes)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        rvLinkInteres_link.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)

        validateLinksAunth()

    }

    private fun validateLinksAunth() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedActivityPublic.observe() was called")
                        Log.w(LOG, "sendRequest() was called")
                        sendRequest()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
            Log.w(LOG, "controlViewModel.getDataFromRoom() was called")
        }
    }

    private fun sendRequest(){
        getLinkInteres()
    }

    private fun getLinkInteres() {


        val usuarioActual = ControlUsuario.instance.currentUsuario[0]
        val listaLinkInteres = ArrayList<Link>()

        /*when (usuarioActual) {
            is Alumno -> {
                if (usuarioActual.tipoAlumno == Utilitarios.POS) {
                    //listaLinkInteres.add(Link("ESAN Virtual", R.drawable.tab_link,"https://esanvirtual.edu.pe/login/index.php"))
                    listaLinkInteres.add(Link("Portal Académico", R.drawable.tab_link,"https://pa.uesan.edu.pe/"))
                } else {
                    //listaLinkInteres.add(Link("UE Virtual", R.drawable.tab_link,"https://uevirtual.ue.edu.pe/intranet/"))
                    listaLinkInteres.add(Link("Portal Académico", R.drawable.tab_link,"https://pa.uesan.edu.pe/"))
                }
            }
            is Profesor -> {
                //listaLinkInteres.add(Link("UE Virtual", R.drawable.tab_link,"https://uevirtual.ue.edu.pe/intranet/"))
                //listaLinkInteres.add(Link("ESAN Virtual", R.drawable.tab_link,"https://esanvirtual.edu.pe/login/index.php"))
                listaLinkInteres.add(Link("Portal Académico", R.drawable.tab_link,"https://pa.uesan.edu.pe/"))
            }
            else -> {
                //listaLinkInteres.add(Link("UE Virtual", R.drawable.tab_link,"https://uevirtual.ue.edu.pe/intranet/"))
                //listaLinkInteres.add(Link("ESAN Virtual", R.drawable.tab_link,"https://esanvirtual.edu.pe/login/index.php"))
                listaLinkInteres.add(Link("Portal Académico", R.drawable.tab_link,"https://pa.uesan.edu.pe/"))
            }
        }*/

        /*listaLinkInteres.add(Link("Career Center", R.drawable.tab_link,"https://careercenter.esan.edu.pe/"))*/

        listaLinkInteres.add(Link("Portal Académico", R.drawable.tab_link,"https://pa.uesan.edu.pe/"))

        /*listaLinkInteres.add(Link("CENDOC", R.drawable.tab_link,"https://esancendoc.esan.edu.pe/"))*/
        listaLinkInteres.add(Link("CENDOC", R.drawable.tab_link,"https://biblioteca.uesan.edu.pe/"))

        when (usuarioActual){
            is Alumno -> {
                if (usuarioActual.tipoAlumno == Utilitarios.POS) {
                    listaLinkInteres.add(Link("Alumni", R.drawable.tab_link,"https://esanalumni.esan.edu.pe/"))
                } else {
                    listaLinkInteres.add(Link("Bolsa de Trabajo", R.drawable.tab_link,"https://bolsatrabajo.ue.edu.pe/"))
                }
            }
            else ->{
                listaLinkInteres.add(Link("Alumni", R.drawable.tab_link,"https://esanalumni.esan.edu.pe/"))
            }
        }

        /*listaLinkInteres.add(Link("Libro de Reclamaciones", R.drawable.tab_link,"https://intranet.esan.edu.pe/SistemasEsan/LibroReclamaciones.nsf"))*/
        /*listaLinkInteres.add(Link("Facturación Electrónica", R.drawable.tab_link,"https://consulta.webfactura.pe/aplicaciones/documentos/documento.nsf"))*/


        listaLinkInteres.add(Link("Libro de Reclamaciones", R.drawable.tab_link,"http://intranet.esan.edu.pe/SistemasEsan/LibroReclamaciones.nsf"))
        listaLinkInteres.add(Link("Facturación Electrónica", R.drawable.tab_link,"http://consulta.webfactura.pe/aplicaciones/documentos/documento.nsf"))

        rvLinkInteres_link.adapter = LinkInteresAdapter(listaLinkInteres)
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

    override fun onStop() {
        Log.w(LOG,"onStop()")
        if (ControlUsuario.instance.currentUsuario.size != 0  && ControlUsuario.instance.currentUsuarioGeneral != null) {
            controlViewModel.insertDataToRoom()
            Log.w(LOG,"onStop() with insertDataToRoom()")
        }
        super.onStop()
    }
}
