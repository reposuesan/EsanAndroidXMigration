package pe.edu.esan.appostgrado.view.academico.postgrado

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_asistencia_post.view.*
import pe.edu.esan.appostgrado.R

/**
 * Created by lventura on 31/07/18.
 */
class AsistenciaPostDialog: AppCompatDialogFragment() {

    private var curso: String = ""
    private var porcInasistencia: Int = 0
    private var faltas: Int = 0
    private var asistencias: Int = 0
    private var tardanzas: Int = 0
    private var totalsesiones: Int = 0

    private var anchoPantalla: Int = 0
    private var densidad: Float = 0f

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (arguments != null) {
            curso = requireArguments()["Curso"] as String
            porcInasistencia = requireArguments()["PorcxInasistencia"] as Int
            faltas = requireArguments()["CantFalta"] as Int
            asistencias = requireArguments()["CantAsistencias"] as Int
            tardanzas = requireArguments()["CantTardanzas"] as Int
            totalsesiones = requireArguments()["CantidadSesiones"] as Int
            anchoPantalla = requireArguments()["AnchoPantalla"] as Int
            densidad = requireArguments()["Densidad"] as Float
        }
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.activity_asistencia_post, null)

        builder.setView(view)

        var porcAsistencia = 0.0f
        var porcTardanza = 0.0f
        var porcFalta = 0.0f
        var porcRestante = 100.0f

        if(totalsesiones > 0) {
            //porcAsistencia = String.format("%.1f", asistencias.toFloat() * 100 / totalsesiones.toFloat()).toFloat()
            porcAsistencia = (asistencias.toFloat() * 100) / (totalsesiones.toFloat())
            //porcTardanza = String.format("%.1f", tardanzas.toFloat() * 100 / totalsesiones.toFloat()).toFloat()
            porcTardanza = (tardanzas.toFloat() * 100) / (totalsesiones.toFloat())
            //porcFalta = String.format("%.1f", faltas.toFloat() * 100 / totalsesiones.toFloat()).toFloat()
            porcFalta = (faltas.toFloat() * 100) / (totalsesiones.toFloat())
            porcRestante = 100 - (porcAsistencia + porcTardanza + porcFalta)
        }

        view.lblCurso_asispost.text = curso
        /*view.lblAsistencia_asispost.text = String.format(resources.getString(R.string.asistencia_cant_porc), asistencias, porcAsistencia)
        view.lblTardanza_asispost.text = String.format(resources.getString(R.string.tardanza_cant_porc), tardanzas, porcTardanza)
        view.lblFalta_asispost.text = String.format(resources.getString(R.string.falta_cant_porc), faltas, porcFalta)*/

        /*view.lblAsistencia_asispost.text = "Asistencias\n9 (27.3%)"
        view.lblTardanza_asispost.text = "Tardanzas\n1 (10.0%)"
        view.lblFalta_asispost.text = "Faltas\n0 (0.0%)"*/

        view.lblAsistencia_asispost.text = resources.getString(R.string.asistencias_postgrado_curso, asistencias, porcAsistencia)
        view.lblTardanza_asispost.text = resources.getString(R.string.tardanzas_postgrado_curso, tardanzas, porcTardanza)
        view.lblFalta_asispost.text = resources.getString(R.string.faltas_postgrado_curso, faltas, porcFalta)

        val valor = 30 * densidad + 0.5f
        val largoTotal = anchoPantalla - (valor.toInt() * 2)

        val widthAsis = largoTotal * porcAsistencia / 100
        val widthTard = largoTotal * porcTardanza / 100
        val widthFalta = largoTotal * porcFalta / 100
        val widthRest = largoTotal * porcRestante / 100

        val widthTardReal = widthAsis + widthTard
        val widthFaltReal = widthTardReal + widthFalta

        view.viewAsistencia_asispost.layoutParams.width = widthAsis.toInt()
        view.viewTardanza_asispost.layoutParams.width = widthTardReal.toInt()
        view.viewFalta_asispost.layoutParams.width = widthFaltReal.toInt()

        return builder.create()
    }
}