package pe.edu.esan.appostgrado.adapter

import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.crashlytics.FirebaseCrashlytics
/*import com.crashlytics.android.Crashlytics*/
import kotlinx.android.synthetic.main.item_cursopost_curso.view.*
import kotlinx.android.synthetic.main.item_cursopost_modulo.view.*
import kotlinx.android.synthetic.main.item_cursopost_nota.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.CursoPostModulo
import pe.edu.esan.appostgrado.entidades.CursosPost
import pe.edu.esan.appostgrado.entidades.NotasPost
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.academico.DirectorioActivity

/**
 * Created by lventura on 7/05/18.
 */
class CursoPostAdapter (val listaModuloCurso: List<CursoPostModulo>, val clickAsistencia: (codigoSeccion: String) -> Unit, val clickListener: (posAdapter: Int, posModulo: Int, posCurso: Int, CursosPost, Boolean) -> Unit ): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val MODULO : Int = 0
    private val CURSO : Int = 1
    private val NOTA : Int = 2

    private val LOG = CursoPostAdapter::class.simpleName


    override fun getItemViewType(position: Int): Int {
        var contador = 0
        for (modulo in 0 until listaModuloCurso.size) {
            if (contador == position) {
                return MODULO
            }
            for (cursoONotaEnModulo in 0 until listaModuloCurso[modulo].listaCursos.size) {
                contador++
                if (contador == position) {
                    if (listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo] == null) {
                        return NOTA
                    } else {
                        return CURSO
                    }
                }
            }
            contador++
        }
        return 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        if (viewType == MODULO) {
            return ModuloViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_cursopost_modulo, parent, false))
        } else if (viewType == CURSO) {
            return CursoViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_cursopost_curso, parent, false))
        } else /*if (viewType == NOTA)*/ {
            return NotaViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_cursopost_nota, parent, false))
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ModuloViewHolder -> {
                val objeto = getObjectReal(holder.adapterPosition)
                when (objeto?.objReal) {
                    is CursoPostModulo -> {
                        holder.setValores(String.format(holder.itemView.context.resources.getString(R.string.modulo_), objeto.objReal.modulo))
                    }
                }
            }
            is CursoViewHolder -> {
                val objeto = getObjectReal(holder.adapterPosition)
                when (objeto?.objReal) {
                    is CursosPost -> {
                        holder.setValores(objeto.modulePosition, objeto.cursoPosition, objeto.objReal, clickListener , clickAsistencia)
                    }
                }
            }
            is NotaViewHolder -> {
                val objeto = getObjectReal(holder.adapterPosition)
                when (objeto?.objReal) {
                    is NotasPost -> {
                        holder.setValores(objeto.objReal)
                    }
                }
            }
        }
    }

    private fun getObjectReal(position: Int): CursoPostReturnReal? {

        var contador = 0
        for (modulo in 0 until listaModuloCurso.size) {

            if (contador == position) {
                return CursoPostReturnReal(listaModuloCurso[modulo], modulo, 0)
            }

            for (cursoONotaEnModulo in 0 until listaModuloCurso[modulo].listaCursos.size) {
                contador++
                if (contador == position) {
                    if (listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo] == null) {
                        var contDet = 0
                        do {
                            contDet++
                        } while (listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo-contDet] == null)

                        val crashlytics = FirebaseCrashlytics.getInstance()

                        try {
                            Log.w(LOG,"Value in bug is: ${listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo-contDet]?.detalleNotas?.get(contDet-1)}")
                        } catch (e: Exception){
                            try {
                                Log.w(LOG,"First list value is: ${listaModuloCurso[modulo]}")

                                try {
                                    Log.w(LOG,"Second list value is: ${listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo-contDet]}")

                                    try {
                                        Log.w(LOG,"Third list value is: ${listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo-contDet]?.detalleNotas?.get(contDet-1)}")
                                    } catch (e: Exception){
                                        crashlytics.log("E/CursoPostAdapter: Value of modulo is: $modulo")
                                        crashlytics.log("E/CursoPostAdapter: Size of listaModuloCurso is: ${listaModuloCurso.size}")
                                        crashlytics.log("E/CursoPostAdapter: Value of cursoONotaEnModulo is: $cursoONotaEnModulo")
                                        crashlytics.log("E/CursoPostAdapter: Value of contDet is: $contDet")
                                        crashlytics.log("E/CursoPostAdapter: Size of listaModuloCurso[modulo].listaCursos is: ${listaModuloCurso[modulo].listaCursos.size}")
                                        crashlytics.log("E/CursoPostAdapter: Size of listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo-contDet]?.detalleNotas? is: ${listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo-contDet]?.detalleNotas?.size}")
                                    }

                                } catch (e: Exception){
                                    crashlytics.log("E/CursoPostAdapter: Value of modulo is: $modulo")
                                    crashlytics.log("E/CursoPostAdapter: Size of listaModuloCurso is: ${listaModuloCurso.size}")
                                    crashlytics.log("E/CursoPostAdapter: Value of cursoONotaEnModulo is: $cursoONotaEnModulo")
                                    crashlytics.log("E/CursoPostAdapter: Value of contDet is: $contDet")
                                    crashlytics.log("E/CursoPostAdapter: Size of listaModuloCurso[modulo].listaCursos is: ${listaModuloCurso[modulo].listaCursos.size}")
                                }
                            } catch (e: Exception){
                                crashlytics.log("E/CursoPostAdapter: Value of modulo is: $modulo")
                                crashlytics.log("E/CursoPostAdapter: Size of listaModuloCurso is: ${listaModuloCurso.size}")
                            }
                        }

                        return CursoPostReturnReal(listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo-contDet]?.detalleNotas?.get(contDet-1), 0, 0)
                    } else {
                        return CursoPostReturnReal(listaModuloCurso[modulo].listaCursos[cursoONotaEnModulo], modulo, cursoONotaEnModulo)
                    }
                }
            }
            contador++
        }
        return null
    }

    override fun getItemCount(): Int {
        var cant = 0
        for (i in 0 until listaModuloCurso.size) {
            cant += listaModuloCurso[i].listaCursos.size
        }
        return cant + listaModuloCurso.size
    }





    private class CursoPostReturnReal (val objReal: Any?, val modulePosition: Int, val cursoPosition: Int)

    class ModuloViewHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(modulo: String) {
            itemView.lblModulo_icursopostmodulo.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblModulo_icursopostmodulo.text = modulo
        }
    }





    class CursoViewHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(posModulo: Int, posCurso: Int, curso: CursosPost, clickListener: (Int, Int, Int, CursosPost, Boolean) -> Unit , clickAsistencia: (codigoSeccion: String) -> Unit) {

            itemView.lblCursoPos_icursopostcurso.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblNota_icursopostcurso.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblCursoPos_icursopostcurso.text = curso.cursoNombre
            itemView.lblNota_icursopostcurso.text = if (curso.promedioFinal == "") curso.promedioCondicion else curso.promedioFinal
            if (curso.cargando) {
                itemView.pbCargando_icursopostcurso.visibility = View.VISIBLE
            } else {
                itemView.pbCargando_icursopostcurso.visibility = View.INVISIBLE
            }

            if (curso.expandible) {
                itemView.imgIndicator_icursopostcurso.setImageResource(R.drawable.icon_arrow_up)
            } else {
                itemView.imgIndicator_icursopostcurso.setImageResource(R.drawable.icon_arrow_down)
            }

            if (curso.encuesta) {
                itemView.lblEncuesta_icursopostcurso.visibility = View.VISIBLE
                itemView.viewNota_icursopostcurso.visibility = View.GONE
            } else {
                itemView.lblEncuesta_icursopostcurso.visibility = View.GONE
                itemView.viewNota_icursopostcurso.visibility = View.VISIBLE
            }

            itemView.setOnClickListener { clickListener(adapterPosition, posModulo, posCurso, curso, curso.encuesta) }

            itemView.viewListaParticipantes_icursopostcurso.setOnClickListener {
                val intentContacto = Intent(itemView.context, DirectorioActivity::class.java)
                intentContacto.putExtra("CodSeccion", curso.seccionCodigo)
                itemView.context.startActivity(intentContacto)
            }

            itemView.viewAsistencia_icursopostcurso.setOnClickListener {
                clickAsistencia(curso.seccionCodigo)
            }

        }
    }





    class NotaViewHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(nota: NotasPost) {
            itemView.lblTipoNota_icursopostnota.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblTipoNota_icursopostnota.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT_ITALIC)
            itemView.lblPeso_icursopostnota.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)

            itemView.lblTipoNota_icursopostnota.text = nota.tipo
            itemView.lblNota_icursopostnota.text = nota.nota
            itemView.lblPeso_icursopostnota.text = nota.peso
        }
    }
}