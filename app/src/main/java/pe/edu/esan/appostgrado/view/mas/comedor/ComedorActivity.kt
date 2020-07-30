package pe.edu.esan.appostgrado.view.mas.comedor

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_comedor.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.ComedorAdapter
import pe.edu.esan.appostgrado.entidades.Comedor
import pe.edu.esan.appostgrado.util.Utilitarios

class ComedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comedor)

        val toolbar = main_comedor as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.comedor)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        getComedor()
    }

    private fun getComedor() {
        val listaComedores = ArrayList<Comedor>()
        listaComedores.add(Comedor("1", "Charlotte", R.drawable.icon_charlotte))
        listaComedores.add(Comedor("2", "Deli Sabores", R.drawable.icon_delisabores))
        listaComedores.add(Comedor("3", "Cafetería La Lupe", R.drawable.la_lupe_restaurant))

        rvComedor_comedor.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvComedor_comedor.adapter = ComedorAdapter(listaComedores)
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
