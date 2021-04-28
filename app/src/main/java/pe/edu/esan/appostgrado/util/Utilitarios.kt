package pe.edu.esan.appostgrado.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.nfc.FormatException
import android.os.Build
import android.util.Base64
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.BuildConfig
import pe.edu.esan.appostgrado.R
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by lventura on 17/04/18.
 */
class Utilitarios {

    enum class TipoFila {
        CABECERA,
        DETALLE
    }

    enum class TipoPreguntaEncuesta {
        CABECERA,
        PREGUNTAUNO,
        PREGUNTADOS
    }

    enum class TipoSeparacion {
        DIAGONAL,
        GUION
    }

    enum class TipoClick {
        OnCLick,
        OnLongClick
    }

    /** Urls
     */
    enum class URL {
        LOGIN,
        HORARIO_NEW,
        CURSOS_PRE,
        SECCIONES,
        PROGRAMAS,
        PAGO_PRE,
        PAGO_POST,
        CURSOS_POST,
        NOTAS_POST,
        ASIS_PROF,
        ASIS_ALUMNO_PRE,
        ASIS_ALUMNO_POST,
        REG_ASIS_PROF,
        PR_PROMOCION,
        PR_CONFIGURACION,
        PR_DETALLEGRUPO,
        PR_DISPONIBILIDAD,
        PR_OTROSHORARIOS,
        PR_REGISTRAR_RESERVA,
        PR_CONFIRMAR_RESERVA,
        PR_MIS_RESERVAS,
        PR_VERIFICAR_GRUPO,
        PR_MI_GRUPO,
        PR_ELIMINAR_ALUMNOGRUPO,
        PR_ALUMNOS_SINGRUPO,
        PR_AGREGAR_ALUMNOGRUPO,
        MALLA_CURRICULAR,
        MALLA_CURRICULAR_RESUMEN,
        VALIDA_ENCUESTA_PROGRAMA,
        LISTA_ASISTENCIA,
        REGISTRAR_ASISTENCIA_ALUMNO,
        REGISTRAR_ASISTENCIA_MASIVA,
        PREGUNTAS_ENCUESTA,
        CARGA_ACADEMICA,
        DIRECTORIO,
        REGISTRAR_ENCUESTA,
        RECOR_ASISTENCIA_ALUMNO,
        RECOR_ASISTENCIA_PROFESOR,
        HISTORICO_NOTAS_PRE,
        RESULTADO_ENCUESTA_CABECERA,
        RESULTADO_ENCUESTA_DETALLE,
        CANTIDAD_MENSAJES,
        MENSAJE,
        MENSAJE_MARCARCOMOLEIDO,
        CERRAR_SESION,
        VERSION_CODE,
        PREG_PP_OBT_CONFIGURACION,
        PREG_PP_VERIFICAR_GRUPO_ALUMNOS,
        PREG_PP_REGISTRA_PRERESERVA,
        PREG_PP_CONFIRMAR_PRERESERVA,
        PREG_PP_LISTA_PRERESERVAS,
        PREG_PP_LISTA_ALUMNOS_X_GRUPO,
        PREG_PP_VERIFICAR_TIPO_POLITICA,
        PREG_PP_ACEPTAR_TIPO_POLITICA,
        PREG_LAB_VERIFICAR_ALUMNO_RESERVA,
        PREG_LAB_CONSULTAR_PROG_X_HORARIO,
        PREG_LAB_REGISTRAR_PRERESERVA_LABORATORIO,
        PREG_LAB_CONFIRMAR_PRERESERVA_LABORATORIO,
        PREG_LAB_LISTAR_PRERESERVAS_LAB_X_ALUMNO,
        PREG_LAB_VERIFICAR_TIPO_POLITICA,
        PREG_LAB_ACEPTAR_TIPO_POLITICA,


        MENU_COMEDOR,
        RA_FACULTADES,
        RA_EDIFICIOS,
        RA_CAFETERIAS,
        RA_DEPORTES,
        RA_BIBLIOTECAS,
        RA_LABORATORIOS,
        RA_AUDITORIOS,
        RA_LIBRERIAS,
        RA_OFICINAS,
        RA_AULAS,
        RA_VARIOS,
        RA_BYID,
        RA_IMAGEN
    }

    /** Tipos de fuente ROBOTO
     */
    enum class TypeFont {
        THIN,
        THIN_ITALIC,
        LIGHT,
        LIGHT_ITALIC,
        REGULAR,
        REGULAR_ITALIC,
        MEDIUM,
        MEDIUM_ITALIC,
        BOLD,
        BOLD_ITALIC,
        BLACK,
        BLACK_ITALIC
    }

    companion object {
        const val horarioSemanaVistaMaxima = 3
        const val PRE = "pre"
        const val POS = "pos"
        const val DOC = "docente"
        const val ALU = "alumno"
        const val mostrarDetalleMarker = false

        private const val ReleasePortal = true
        private const val Release = true

        private const val DominioPortal = "https://movilrestws.esan.edu.pe"
        private const val DominioPortalTest = "https://qasmovilrestws.esan.edu.pe"

        private const val DominioUE = "https://intranetmovil.ue.edu.pe"
        private const val DominioUETest = "https://intranetmovil.ue.edu.pe"

        private const val DominioESAN = "https://intranetmovil.esan.edu.pe"
        private const val DominioESANTest = "https://intranetmovil.esan.edu.pe"

        fun getUrl(url: URL) : String {

            when (url) {
                URL.LOGIN ->                        return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/login/AutenticarUsuario"
                URL.HORARIO_NEW ->                  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/horario/GestionAcademica/HorarioFecha"
                URL.CURSOS_PRE ->                   return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notas/GestionAcademica/ListarNotasActualesAlumnoPre"
                URL.SECCIONES ->                    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/horario/GestionAcademica/SeccionesXProfesor"
                URL.PROGRAMAS ->                    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notas/GestionAcademica/ListarProgramasPost"
                URL.PAGO_PRE ->                     return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/pagos/GestionAcademica/CronogramaPagosPregrado"
                URL.PAGO_POST ->                    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/pagos/GestionAcademica/ListaProgramacionPagosxAlumnoPosgrado"
                URL.CURSOS_POST ->                  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notas/GestionAcademica/ListarHistoricoNotasAlumnoPost"
                URL.NOTAS_POST ->                   return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notas/GestionAcademica/ListarDetalleNotaHistorico"
                URL.ASIS_PROF ->                    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/ConsultarAsistenciaProfesor"
                URL.ASIS_ALUMNO_PRE ->              return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/ResumenAsistenciaAlumnoPregrado"
                URL.ASIS_ALUMNO_POST ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/ObtenerAsistenciaAlumnoSeccion"
                URL.REG_ASIS_PROF ->                return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/RegistrarAsistenciaProfesor"
                URL.PR_PROMOCION ->                 return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ListarPromocionxAlumno"
                URL.PR_CONFIGURACION ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ListarConfiguracionPromocionxAlumno"
                URL.PR_DETALLEGRUPO ->              return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ListarHorasxAlumno"
                URL.PR_DISPONIBILIDAD ->            return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/DisponibilidadSala"
                URL.PR_OTROSHORARIOS ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ListarDisponibilidadOtrasSala"
                URL.PR_REGISTRAR_RESERVA ->         return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/RegistraReservaAmbiente"
                URL.PR_CONFIRMAR_RESERVA ->         return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ConfirmarReservaAlumno"
                URL.PR_MIS_RESERVAS ->              return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ListarReservasxConfiguracionAlumno"
                URL.PR_VERIFICAR_GRUPO ->           return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/grupos/GestionAmbiente/VerificarGrupo"
                URL.PR_MI_GRUPO ->                  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/grupos/GestionAmbiente/ListarAlumnoDeGrupo"
                URL.PR_ELIMINAR_ALUMNOGRUPO ->      return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/grupos/GestionAmbiente/EliminarAlumnoGrupo"
                URL.PR_ALUMNOS_SINGRUPO ->          return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/grupos/GestionAmbiente/ListarAlumnosSinGrupo"
                URL.PR_AGREGAR_ALUMNOGRUPO ->       return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/grupos/GestionAmbiente/RegistraAlumnoGrupo"
                URL.MALLA_CURRICULAR ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notas/GestionAcademica/ListarAvanceCurricularAlumnoPre"
                URL.MALLA_CURRICULAR_RESUMEN ->     return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notas/GestionAcademica/ListarResumenAcademicoAlumnoPre"
                URL.VALIDA_ENCUESTA_PROGRAMA ->     return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notas/GestionAcademica/ListarEncuestasProfesorPost"
                URL.LISTA_ASISTENCIA ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/ListarAsistenciaAlumnosxSeccion"
                URL.REGISTRAR_ASISTENCIA_ALUMNO ->  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/RegistrarAsistenciaAlumno"
                URL.REGISTRAR_ASISTENCIA_MASIVA ->  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/RegistrarAsistenciaMasivaAlumno"
                URL.PREGUNTAS_ENCUESTA ->           return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/encuesta/GestionAcademica/ListarEncuestaPregunta"
                URL.CARGA_ACADEMICA ->              return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/carga/GestionAcademica/ListarCargaAcademica"
                URL.DIRECTORIO ->                   return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/directorio/GestionAcademica/ListaDirectorioAlumno"
                URL.REGISTRAR_ENCUESTA ->           return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/encuesta/GestionAcademica/RegistrarEncuesta"
                URL.RECOR_ASISTENCIA_ALUMNO ->      return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/ListarRecordAsistenciaAlumno"
                URL.RECOR_ASISTENCIA_PROFESOR ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/asistencia/GestionAsistencia/ListarRecordAsistenciaDocente"
                URL.HISTORICO_NOTAS_PRE ->          return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notas/GestionAcademica/ListarHistoricoNotasAlumnoPre"
                URL.RESULTADO_ENCUESTA_CABECERA ->  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/encuesta/GestionAcademica/ObtenerEncuestaPorProfesor"
                URL.RESULTADO_ENCUESTA_DETALLE ->   return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/encuesta/GestionAcademica/EncuestaDetallePorProfesor"
                URL.CANTIDAD_MENSAJES ->            return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notificacion/GestionNotificaciones/NotificacionPendientePorUsuario"
                URL.MENSAJE ->                      return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notificacion/GestionNotificaciones/ListarNotificacionPorUsuario"
                URL.MENSAJE_MARCARCOMOLEIDO ->      return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/notificacion/GestionNotificaciones/DesactivarNotificacionPorId"
                URL.CERRAR_SESION ->                return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/acceso/GestionSeguridad/ActualizarToken"
                URL.VERSION_CODE ->                 return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/login/ObtenerVersionMovil"

                //PREGRADO SALAS DE ESTUDIO
                URL.PREG_PP_OBT_CONFIGURACION ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ObtenerConfiguracionAlumno"
                URL.PREG_PP_VERIFICAR_GRUPO_ALUMNOS ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/VerificarGrupoAlumnosReserva"
                URL.PREG_PP_REGISTRA_PRERESERVA ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/RegistraPreReserva"
                URL.PREG_PP_CONFIRMAR_PRERESERVA ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ConfirmarPreReserva"
                URL.PREG_PP_LISTA_PRERESERVAS ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ListaPreReservasxAlumno"
                URL.PREG_PP_LISTA_ALUMNOS_X_GRUPO ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/salas/GestionAmbiente/ListaAlumnosxGrupo"
                URL.PREG_PP_VERIFICAR_TIPO_POLITICA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/politica/GestionSeguridad/VerificarTipoPoliticaPortal"
                URL.PREG_PP_ACEPTAR_TIPO_POLITICA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/politica/GestionSeguridad/AceptarTipoPolitica"

                //PREGRADO LABORATORIOS
                URL.PREG_LAB_VERIFICAR_ALUMNO_RESERVA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/laboratorio/GestionAmbiente/VerificarAlumnoReserva"
                URL.PREG_LAB_CONSULTAR_PROG_X_HORARIO -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/laboratorio/GestionAmbiente/ConsultarProgramasxHorario"
                URL.PREG_LAB_REGISTRAR_PRERESERVA_LABORATORIO -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/laboratorio/GestionAmbiente/RegistrarPreReservaLaboratorio"
                URL.PREG_LAB_CONFIRMAR_PRERESERVA_LABORATORIO -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/laboratorio/GestionAmbiente/ConfirmarPreReservaLaboratorio"
                URL.PREG_LAB_LISTAR_PRERESERVAS_LAB_X_ALUMNO -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/laboratorio/GestionAmbiente/ListarPreReservasLabxAlumno"
                URL.PREG_LAB_VERIFICAR_TIPO_POLITICA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/politica/GestionSeguridad/VerificarTipoPoliticaPortal"
                URL.PREG_LAB_ACEPTAR_TIPO_POLITICA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/api/politica/GestionSeguridad/AceptarTipoPolitica"

                URL.MENU_COMEDOR ->                 return (if (Release) DominioUE else DominioUETest) + "/Service/AlumnoService.svc/menuComedor/"

                //REALIDAD AUMENTADA
                URL.RA_FACULTADES ->                return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/facultades"
                URL.RA_EDIFICIOS ->                 return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/edificios"
                URL.RA_CAFETERIAS ->                return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/cafeterias"
                URL.RA_DEPORTES ->                  return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/deportes"
                URL.RA_BIBLIOTECAS ->               return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/bibliotecas"
                URL.RA_LABORATORIOS ->              return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/laboratorios"
                URL.RA_AUDITORIOS ->                return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/auditorios"
                URL.RA_LIBRERIAS ->                 return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/librerias"
                URL.RA_OFICINAS ->                  return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/oficinas"
                URL.RA_AULAS ->                     return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/aulas"
                URL.RA_VARIOS ->                    return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambientegrupo/varios"
                URL.RA_BYID ->                      return (if (Release) DominioESAN else DominioESANTest) + "/DataUsuario.svc/ambiente/"
                URL.RA_IMAGEN ->                    return (if (Release) DominioESAN else DominioESANTest) + "/recursos/img/"
                else -> return ""
            }

        }

        /** Obtener la url de la foto del usuario
         * @param codigo representa el código del alumnos o profesor
         * @param size representa el tamaño de imagen se recomienta valores 140 y 80
         * @return String URL
         */
        fun getUrlFoto(codigo: String, size: Int): String {
            return "https://fotos.ue.edu.pe/Inicio/MostrarFotoActorxCodigo/?codigo=$codigo&width=$size"
            /*return "https://qasfotos.ue.edu.pe/Inicio/MostrarFotoActorxCodigo/?codigo=$codigo&width=$size"*/
        }

        /** Obtener la url para generar código de barras a partir del codigo del usuario
         * @param codigo representa el código del alumnos o profesor
         * @return String URL
         */
        fun getCodeBarUrl(codigo: String): String {
            return "https://barcode.tec-it.com/barcode.ashx?bgcolor=FFFFFF&code=Code39FullASCII&color=CF1111&data=$codigo&dpi=96"
            /*return "https://bwipjs-api.metafloor.com/?bcid=code128&text=$codigo&backgroundcolor=FFFFFF&textcolor=CF1111&barcolor=CF1111&scaleX=16&scaleY=4&includetext"*/
        }

        /** Obtener la url de la boleta de pago de los alumnos (Solo Pregrado)
         * @param codigo representa el código de la boleta de pago
         * @return String URL
         */
        fun getBoletasPreUrl(codigo: String): String {
            return "https://recibos.ue.edu.pe/recibos/$codigo.pdf"
        }

        fun getQRUrl(size: Int, valor: String): String {
            return "https://chart.googleapis.com/chart?cht=qr&chs=${size}x${size}&chl=$valor"
        }

        //FONTS
        /** Obtener la fuente ROBOTO
         * @param context cualquier context
         * @param type tipo de fuente deseada
         * @see TypeFont
         * @return fuente
         */
        fun getFontRoboto(context: Context, type: TypeFont) : Typeface {
            return when (type) {
                TypeFont.THIN -> Typeface.createFromAsset(context.assets, "fonts/Roboto-Thin.ttf")
                TypeFont.THIN_ITALIC -> Typeface.createFromAsset(context.assets, "fonts/Roboto-ThinItalic.ttf")
                TypeFont.LIGHT -> Typeface.createFromAsset(context.assets, "fonts/Roboto-Light.ttf")
                TypeFont.LIGHT_ITALIC -> Typeface.createFromAsset(context.assets, "fonts/Roboto-LightItalic.ttf")
                TypeFont.REGULAR -> Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")
                TypeFont.REGULAR_ITALIC -> Typeface.createFromAsset(context.assets, "fonts/Roboto-Italic.ttf")
                TypeFont.MEDIUM -> Typeface.createFromAsset(context.assets, "fonts/Roboto-Medium.ttf")
                TypeFont.MEDIUM_ITALIC -> Typeface.createFromAsset(context.assets, "fonts/Roboto-MediumItalic.ttf")
                TypeFont.BOLD -> Typeface.createFromAsset(context.assets, "fonts/Roboto-Bold.ttf")
                TypeFont.BOLD_ITALIC -> Typeface.createFromAsset(context.assets, "fonts/Roboto-BoldItalic.ttf")
                TypeFont.BLACK -> Typeface.createFromAsset(context.assets, "fonts/Roboto-Black.ttf")
                TypeFont.BLACK_ITALIC -> Typeface.createFromAsset(context.assets, "fonts/Roboto-BlackItalic.ttf")
            }
        }


        //UTILITARIOS

        fun getRoundedCornerDrawable(drawable: Drawable): Bitmap {
            val bitmap = Bitmap.createBitmap(122, 144, Bitmap.Config.ARGB_8888)
            val canvas2 = Canvas(bitmap)
            drawable.setBounds(0, 0, 122, 144)
            drawable.draw(canvas2)

            val output = Bitmap.createBitmap(bitmap.width,
                    bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = -0x10000
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)
            val roundPx = 70f

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }

        fun getDiaSemana(dia: Int) :Int {
            val sDia = dia - 2
            return if (sDia < 0) 6 else sDia
        }

        fun getStringToDateHHmm(hora: String) : Date?{
            return try {
                val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
                hhmm.parse(hora)
            } catch (pe: ParseException) {
                null
            }
        }

        fun getLongToStringHHmm(tiempo: Long): String {
            val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
            return try {
                hhmm.format(tiempo)
            } catch (pex: ParseException) {
                ""
            }
        }

        fun addMinutesToDate(fecha: Date?, minutos: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.time = fecha ?: Date()
            calendar.add(Calendar.MINUTE, minutos)

            return calendar.time
        }

        fun getStringToDateddMMyyyyHHmm(fecha: String): Date? {
            val ddMMyyyyHHmm = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            return try {
                ddMMyyyyHHmm.parse(fecha)
            } catch (e: Exception) {
                null
            }
        }

        fun getStringToDateddMMyyyy(fecha: String): Date {
            return try {
                var newfecha = fecha.replace("/Date(", "")
                newfecha = newfecha.replace(")/", "")
                val `val` = newfecha.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val va = java.lang.Long.parseLong(`val`[0])
                Date(va)
            } catch (e: Exception) {
                Date()
            }
        }

        fun getDateToStringddMMyyyy(fecha: Date): String {
            val ddMMyyyyHHmm = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            return try {
                ddMMyyyyHHmm.format(fecha)
            } catch (e: Exception) {
                ddMMyyyyHHmm.format(Date())
            }
        }

        fun getDateToStringddMMyyyyWithoutHHmm(fecha: Date): String {
            val ddMMyyyyHHmm = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            return try {
                ddMMyyyyHHmm.format(fecha)
            } catch (e: Exception) {
                ddMMyyyyHHmm.format(Date())
            }
        }

        fun getStringToStringddMMyyyyHHmmTwo(fecha: String) : String {
            val ddMMyyyyHHmm = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val f: Date? = try {
                ddMMyyyyHHmm.parse(fecha)
            } catch (e: Exception) {
                Date()
            }

            return ddMMyyyyHHmm.format(f?.time)
        }

        fun getStringToStringddMMyyyyHHmmThree(fecha: String) : String {
            val yyyyMMddTHHmm = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val ddMMyyyyHHmm = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val f = try {
                yyyyMMddTHHmm.parse(fecha)
            } catch (e: Exception) {
                Date()
            }
            return ddMMyyyyHHmm.format(f.time)
        }

        fun getLongToStringddMMyyyy(fecha: Long, separacion: TipoSeparacion): String {
            val ddMMyyyy = when (separacion) {
                TipoSeparacion.DIAGONAL -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                TipoSeparacion.GUION -> SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            }

            return try {
                ddMMyyyy.format(fecha)
            } catch (e: FormatException) {
                ""
            }
        }

        fun getStringToDatedd(fecha: Date) : String {
            val dd = SimpleDateFormat("dd", Locale.getDefault())
            return try {
                dd.format(fecha)
            } catch (e: FormatException) {
                ""
            }
        }

        fun comprobarSensor(context: Context): Boolean {
            val sensorMgr = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            var sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER)
            if (sensors.isEmpty()) {
                return false
            } else {
                sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD)
                if (sensors.isEmpty())
                    return false
            }
            return true
        }

        fun getDia(numeroDia : Int, context: Context): String {
            return when (numeroDia) {
                in 1..7 -> context.resources.getStringArray(R.array.dias_semana)[numeroDia-1]
                else -> ""
            }
        }

        //ENCRIPTACION

        private const val characterEncoding = "UTF-8"
        private const val cipherTransformation = "AES/CBC/PKCS5Padding"
        private const val aesEncryptionAlgorithm = "AES"

        fun jsObjectEncrypted(json : JSONObject, context: Context) : JSONObject? {
            try {
                val stringJSON = json.toString()
                val encriptado = encrypt(stringJSON, String(Base64.decode(BuildConfig.ESAN, Base64.DEFAULT))).replace("\n","")
                val jsObject = JSONObject()
                jsObject.put("request", encriptado)

                return jsObject
            } catch (ue: UnsupportedEncodingException) { }
            catch (ike: InvalidKeyException) { }
            catch (nae: NoSuchAlgorithmException) { }
            catch (iape: InvalidAlgorithmParameterException) { }
            catch (ibe: IllegalBlockSizeException) { }
            catch (bpe: BadPaddingException) { }
            catch (nspe: NoSuchPaddingException) { }
            catch (jex: JSONException) { }

            return null
        }

        fun jsArrayEncrypted(jarray : JSONArray, context: Context) : JSONObject? {
            try {
                val encriptado = encrypt(jarray.toString(), String(Base64.decode(BuildConfig.ESAN, Base64.DEFAULT))).replace("\n","")
                val jsObject = JSONObject()
                jsObject.put("request", encriptado)

                return jsObject
            } catch (ue: UnsupportedEncodingException) { }
            catch (ike: InvalidKeyException) { }
            catch (nae: NoSuchAlgorithmException) { }
            catch (iape: InvalidAlgorithmParameterException) { }
            catch (ibe: IllegalBlockSizeException) { }
            catch (bpe: BadPaddingException) { }
            catch (nspe: NoSuchPaddingException) { }
            catch (jex: JSONException) { }

            return null
        }

        fun jsObjectDesencriptar (valor: String, context: Context) : JSONObject? {
            try {
                val resultado = decrypt(valor, String(Base64.decode(BuildConfig.ESAN, Base64.DEFAULT)).replace("\n",""))

                return JSONObject(resultado)

            } catch (ue: UnsupportedEncodingException) { }
            catch (ike: InvalidKeyException) { }
            catch (nae: NoSuchAlgorithmException) { }
            catch (iape: InvalidAlgorithmParameterException) { }
            catch (ibe: IllegalBlockSizeException) { }
            catch (bpe: BadPaddingException) { }
            catch (nspe: NoSuchPaddingException) { }
            catch (gs: GeneralSecurityException) { }
            catch (io: IOException) { }
            catch (jex: JSONException) { }
            return null
        }

        fun jsArrayDesencriptar (valor: String, context: Context) : JSONArray? {
            try {
                val resultado = decrypt(valor, String(Base64.decode(BuildConfig.ESAN, Base64.DEFAULT)).replace("\n",""))

                if (resultado == "null") {
                    return JSONArray()
                }
                return JSONArray(resultado)

            } catch (ue: UnsupportedEncodingException) { }
            catch (ike: InvalidKeyException) { }
            catch (nae: NoSuchAlgorithmException) { }
            catch (iape: InvalidAlgorithmParameterException) { }
            catch (ibe: IllegalBlockSizeException) { }
            catch (bpe: BadPaddingException) { }
            catch (nspe: NoSuchPaddingException) { }
            catch (gs: GeneralSecurityException) { }
            catch (io: IOException) { }
            catch (jex: JSONException) { }
            return null
        }

        fun stringDesencriptar (valor: String, context: Context) : String? {
            try {
                return decrypt(valor, String(Base64.decode(BuildConfig.ESAN, Base64.DEFAULT)).replace("\n",""))

            } catch (ue: UnsupportedEncodingException) { }
            catch (ike: InvalidKeyException) { }
            catch (nae: NoSuchAlgorithmException) { }
            catch (iape: InvalidAlgorithmParameterException) { }
            catch (ibe: IllegalBlockSizeException) { }
            catch (bpe: BadPaddingException) { }
            catch (nspe: NoSuchPaddingException) { }
            catch (gs: GeneralSecurityException) { }
            catch (io: IOException) { }
            catch (jex: JSONException) { }
            return null
        }

        fun stringEncriptar(input: String): String? {
            try {
                return encrypt(input, String(Base64.decode(BuildConfig.ESAN, Base64.DEFAULT))).replace("\n","")
            } catch (ue: UnsupportedEncodingException) { }
            catch (ike: InvalidKeyException) { }
            catch (nae: NoSuchAlgorithmException) { }
            catch (iape: InvalidAlgorithmParameterException) { }
            catch (ibe: IllegalBlockSizeException) { }
            catch (bpe: BadPaddingException) { }
            catch (nspe: NoSuchPaddingException) { }
            catch (gs: GeneralSecurityException) { }
            catch (io: IOException) { }
            catch (jex: JSONException) { }
            return null

        }

        @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class)
        private fun decrypt(cipherText: ByteArray, key: ByteArray, initialVector: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(cipherTransformation)
            val secretKeySpecy = SecretKeySpec(key, aesEncryptionAlgorithm)
            val ivParameterSpec = IvParameterSpec(initialVector)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec)

            return cipher.doFinal(cipherText)
        }

        @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class)
        private fun encrypt(plainText: ByteArray, key: ByteArray, initialVector: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(cipherTransformation)
            val secretKeySpec = SecretKeySpec(key, aesEncryptionAlgorithm)
            val ivParameterSpec = IvParameterSpec(initialVector)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

            return cipher.doFinal(plainText)
        }

        @Throws(UnsupportedEncodingException::class)
        private fun getKeyBytes(key: String): ByteArray {
            val keyBytes = ByteArray(16)
            val parameterKeyBytes = key.toByteArray(charset(characterEncoding))
            System.arraycopy(parameterKeyBytes, 0, keyBytes, 0, Math.min(parameterKeyBytes.size, keyBytes.size))
            return keyBytes
        }

        @Throws(UnsupportedEncodingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class)
        private fun encrypt(plainText: String, key: String): String {
            val plainTextbytes = plainText.toByteArray(charset(characterEncoding))
            val keyBytes = getKeyBytes(key)
            return Base64.encodeToString(encrypt(plainTextbytes, keyBytes, keyBytes), Base64.DEFAULT)
        }

        @Throws(KeyException::class, GeneralSecurityException::class, GeneralSecurityException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class, IOException::class)
        private fun decrypt(encryptedText: String, key: String): String {
            val cipheredBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val keyBytes = getKeyBytes(key)
            return String(decrypt(cipheredBytes, keyBytes, keyBytes), charset(characterEncoding))
        }
    }
}

fun isOnlineUtils(context: Context): Boolean{
    val connectivityManager =  context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if(capabilities != null){
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                    return true
                }
            }
        }
    } else {
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    return false
}

fun Context.getHeaderForJWT(): HashMap<String, String> {

    val misPreferencias = getSharedPreferences(
        "PreferenciasUsuario",
        Context.MODE_PRIVATE
    )

    val jwtToken = misPreferencias.getString("jwt", "")

    val headers = HashMap<String, String>()
    headers["Authorization"] = "Bearer $jwtToken"

    return headers

}

fun Context.renewToken(callbackToken: (jwtNewToken: String?) -> Unit){

    val tag = "RenewToken"

    val misPreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)

    val usuario = misPreferencias.getString("code", "")
    val clave = misPreferencias.getString("password", "")
    val token = misPreferencias.getString("tokenID", "")

    val request = JSONObject()
    request.put("Usuario", usuario)
    request.put("Password", clave)
    request.put("Token", token)

    val fRequest = Utilitarios.jsObjectEncrypted(request, this)
    val url = Utilitarios.getUrl(Utilitarios.URL.LOGIN)

    if (fRequest != null) {
        val requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            fRequest,
            { response ->
                try {
                    if (!response.isNull("AutenticarUsuarioResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(
                            response.getString("AutenticarUsuarioResult"),this
                        )

                        if (jsResponse != null) {
                            val jwtToken = jsResponse["TokenJWT"] as? String?

                            val editor = misPreferencias.edit()

                            if(!jwtToken.isNullOrEmpty()){
                                editor.putString("jwt", jwtToken)
                            } else {
                                editor.putString("jwt", "")
                            }

                            editor.apply()
                            callbackToken(jwtToken)
                        }
                    }
                } catch (jex: JSONException) {
                    val editor = misPreferencias.edit()
                    editor.putString("jwt", "")
                    editor.apply()
                    callbackToken(null)
                }
            },
            {
                val editor = misPreferencias.edit()
                editor.putString("jwt", "")
                editor.apply()
                callbackToken(null)
            }
        )

        jsObjectRequest.retryPolicy = DefaultRetryPolicy(
            15000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        jsObjectRequest.tag = tag

        requestQueue?.add(jsObjectRequest)

    }
}