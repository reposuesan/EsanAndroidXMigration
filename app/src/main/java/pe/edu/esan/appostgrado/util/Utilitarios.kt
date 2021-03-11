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
        HORARIO,
        HORARIO_NEW,
        CURSOS_PRE,
        SECCIONES, //SECCIONES PROFESOR
        PROGRAMAS,
        CURSOS_POST,
        NOTAS_POST,
        PAGO_PRE,
        PAGO_POST,
        ASIS_PROF, // CONSULTAR ASISTENCIA PROFESOR
        ASIS_ALUMNO_PRE,
        ASIS_ALUMNO_POST,
        REG_ASIS_PROF, // REGISTRAR ASISTENCIA PROFESOR
        PR_PROMOCION,
        PR_CONFIGURACION,
        PR_DETALLEGRUPO,
        PR_DISPONIBILIDAD,
        PR_OTROSHORARIOS,
        PR_REGISTRAR_RESERVA,
        PR_CONFIRMAR_RESERVA,
        PR_VERIFICAR_GRUPO,
        PR_MIS_RESERVAS,
        PR_MI_GRUPO,
        PR_ELIMINAR_ALUMNOGRUPO,
        PR_ALUMNOS_SINGRUPO,
        PR_AGREGAR_ALUMNOGRUPO,
        MALLA_CURRICULAR,
        LISTA_ASISTENCIA,
        REGISTRAR_ASISTENCIA_PROFESOR,
        REGISTRAR_ASISTENCIA_ALUMNO,
        REGISTRAR_ASISTENCIA_MASIVA,
        /*REGISTRAR_ASISTENCIA_COPIA,*/
        VALIDA_ENCUESTA_PROGRAMA,
        PREGUNTAS_ENCUESTA,
        REGISTRAR_ENCUESTA,
        CARGA_ACADEMICA,
        DIRECTORIO,
        RECOR_ASISTENCIA_ALUMNO,
        RECOR_ASISTENCIA_PROFESOR,
        HISTORICO_NOTAS_PRE,
        MALLA_CURRICULAR_RESUMEN,
        RESULTADO_ENCUESTA_CABECERA,
        RESULTADO_ENCUESTA_DETALLE,
        CANTIDAD_MENSAJES,
        MENSAJE,
        MENSAJE_MARCARCOMOLEIDO,
        MENU_COMEDOR,
        CERRAR_SESION,
        VERSION_CODE,
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
        RA_IMAGEN,
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
        PREG_LAB_ACEPTAR_TIPO_POLITICA
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
        val horarioSemanaVistaMaxima = 3
        val PRE = "pre"
        val POS = "pos"
        val DOC = "docente"
        val ALU = "alumno"
        val mostrarDetalleMarker = false

        private val ReleasePortal = true
        private val DominioPortal = "https://restws.esan.edu.pe"
        private val DominioPortalTest = "https://devrestws.esan.edu.pe"
        //CONEXION
        fun isNetworkAvailable (context: Context) : Boolean {

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
            return activeNetworkInfo != null
        }

        fun getUrl(url: URL) : String {

            when (url) {
                URL.LOGIN ->                        return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionSeguridad/Acceso/AutenticacionService.svc/AutenticarUsuario"
                URL.HORARIO_NEW ->                  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Horario/Horario.svc/HorarioFecha"
                //URL.HORARIO_NEW ->                return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Horario/Horario.svc/HorarioFecha2"
                URL.CURSOS_PRE ->                   return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Notas/NotasAlumno.svc/ListarNotasActualesAlumnoPre"
                URL.SECCIONES ->                    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Horario/Horario.svc/SeccionesXProfesor"
                URL.PROGRAMAS ->                    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Notas/NotasAlumno.svc/ListarProgramasPost"
                URL.PAGO_PRE ->                     return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Pagos/CronogramaPagos.svc/CronogramaPagosPregrado"
                URL.PAGO_POST ->                    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Pagos/CronogramaPagos.svc/CronogramaPagosPospago"
                URL.CURSOS_POST ->                  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Notas/NotasAlumno.svc/ListarHistoricoNotasAlumnoPost"
                URL.NOTAS_POST ->                   return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Notas/NotasAlumno.svc/ListarDetalleNotaHistorico"
                URL.ASIS_PROF ->                    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/ConsultarAsistenciaProfesor"
                URL.ASIS_ALUMNO_PRE ->              return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/ResumenAsistenciaAlumnoPregrado"
                URL.ASIS_ALUMNO_POST ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/ObtenerAsistenciaAlumnoSeccion"
                URL.REG_ASIS_PROF ->                return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/RegistrarAsistenciaProfesor"
                URL.PR_PROMOCION ->                 return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/Reserva.svc/ListarPromocionxAlumno"
                URL.PR_CONFIGURACION ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/Reserva.svc/ListarConfiguracionPromocionxAlumno"
                URL.PR_DETALLEGRUPO ->              return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/Reserva.svc/ListarHorasxAlumno"
                URL.PR_DISPONIBILIDAD ->            return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/Reserva.svc/DisponibilidadSala"
                URL.PR_OTROSHORARIOS ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/Reserva.svc/ListarDisponibilidadOtrasSala"
                URL.PR_REGISTRAR_RESERVA ->         return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/Reserva.svc/RegistraReserva"
                URL.PR_CONFIRMAR_RESERVA ->         return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/Reserva.svc/ConfirmarReservaAlumno"
                URL.PR_MIS_RESERVAS ->              return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/Reserva.svc/ListarReservasxConfiguracionAlumno"
                URL.PR_VERIFICAR_GRUPO ->           return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/AdministrarGrupos/GruposReserva.svc/VerificarGrupo"
                URL.PR_MI_GRUPO ->                  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/AdministrarGrupos/GruposReserva.svc/ListarAlumnoDeGrupo"
                URL.PR_ELIMINAR_ALUMNOGRUPO ->      return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/AdministrarGrupos/GruposReserva.svc/EliminarAlumnoGrupo"
                URL.PR_ALUMNOS_SINGRUPO ->          return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/AdministrarGrupos/GruposReserva.svc/ListarAlumnosSinGrupo"
                URL.PR_AGREGAR_ALUMNOGRUPO ->       return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/AdministrarGrupos/GruposReserva.svc/RegistraAlumnoGrupo"
                URL.MALLA_CURRICULAR ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Notas/NotasAlumno.svc/ListarAvanceCurricularAlumnoPre"
                URL.MALLA_CURRICULAR_RESUMEN ->     return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Notas/NotasAlumno.svc/ListarResumenAcademicoAlumnoPre"
                URL.VALIDA_ENCUESTA_PROGRAMA ->     return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Notas/NotasAlumno.svc/ListarEncuestasProfesorPost"
                URL.LISTA_ASISTENCIA ->             return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/ListarAsistenciaAlumnosxSeccion"
                /*URL.REGISTRAR_ASISTENCIA_PROFESOR ->return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/RegistrarAsistenciaProfesor"*/
                URL.REGISTRAR_ASISTENCIA_ALUMNO ->  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/RegistrarAsistenciaAlumno"
                URL.REGISTRAR_ASISTENCIA_MASIVA ->  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/RegistrarAsistenciaMasivaAlumno"
                URL.PREGUNTAS_ENCUESTA ->           return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Encuesta/Encuesta.svc/ListarEncuestaPregunta"
                URL.CARGA_ACADEMICA ->              return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Carga/Carga.svc/ListarCargaAcademica"
                URL.DIRECTORIO ->                   return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Directorio/DirectorioAlumno.svc/ListaDirectorioAlumno"
                URL.REGISTRAR_ENCUESTA ->           return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Encuesta/Encuesta.svc/RegistrarEncuesta"
                URL.RECOR_ASISTENCIA_ALUMNO ->      return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/ListarRecordAsistenciaAlumno"
                URL.RECOR_ASISTENCIA_PROFESOR ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAsistencia/ControlAsistencia/Asistencia.svc/ListarRecordAsistenciaDocente"
                URL.HISTORICO_NOTAS_PRE ->          return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Notas/NotasAlumno.svc/ListarHistoricoNotasAlumnoPre"
                URL.RESULTADO_ENCUESTA_CABECERA ->  return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Encuesta/Encuesta.svc/EncuestaPorProfesor"
                URL.RESULTADO_ENCUESTA_DETALLE ->   return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAcademica/Encuesta/Encuesta.svc/EncuestaDetallePorProfesor"
                URL.CANTIDAD_MENSAJES ->            return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionNotificaciones/Notificacion/Notificacion.svc/NotificacionPendientePorUsuario"
                URL.MENSAJE ->                      return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionNotificaciones/Notificacion/Notificacion.svc/NotificacionPorUsuario"
                URL.MENSAJE_MARCARCOMOLEIDO ->      return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionNotificaciones/Notificacion/Notificacion.svc/DesactivarNotificacion"
                URL.CERRAR_SESION ->                return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionSeguridad/Acceso/AutenticacionService.svc/ActualizarToken"
                URL.VERSION_CODE ->                 return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionSeguridad/Acceso/AutenticacionService.svc/ObtenerVersionMovil"

                //PREGRADO SALAS DE ESTUDIO
                URL.PREG_PP_OBT_CONFIGURACION ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/ReservaPre.svc/ObtenerConfiguracionAlumno"
                URL.PREG_PP_VERIFICAR_GRUPO_ALUMNOS ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/ReservaPre.svc/VerificarGrupoAlumnosReserva"
                URL.PREG_PP_REGISTRA_PRERESERVA ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/ReservaPre.svc/RegistraPreReserva"
                URL.PREG_PP_CONFIRMAR_PRERESERVA ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/ReservaPre.svc/ConfirmarPreReserva"
                URL.PREG_PP_LISTA_PRERESERVAS ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/ReservaPre.svc/ListaPreReservasxAlumno"
                URL.PREG_PP_LISTA_ALUMNOS_X_GRUPO ->    return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/SalasCubiculos/ReservaPre.svc/ListaAlumnosxGrupo"
                URL.PREG_PP_VERIFICAR_TIPO_POLITICA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionSeguridad/Politicas/PoliticaUsoPortal.svc/VerificarTipoPoliticaPortal"
                URL.PREG_PP_ACEPTAR_TIPO_POLITICA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionSeguridad/Politicas/PoliticaUsoPortal.svc/AceptarTipoPolitica"

                //PREGRADO LABORATORIOS
                URL.PREG_LAB_VERIFICAR_ALUMNO_RESERVA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/Laboratorios/ReservaPre.svc/VerificarAlumnoReserva"
                URL.PREG_LAB_CONSULTAR_PROG_X_HORARIO -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/Laboratorios/ReservaPre.svc/ConsultarProgramasxHorario"
                URL.PREG_LAB_REGISTRAR_PRERESERVA_LABORATORIO -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/Laboratorios/ReservaPre.svc/RegistrarPreReservaLaboratorio"
                URL.PREG_LAB_CONFIRMAR_PRERESERVA_LABORATORIO -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/Laboratorios/ReservaPre.svc/ConfirmarPreReservaLaboratorio"
                URL.PREG_LAB_LISTAR_PRERESERVAS_LAB_X_ALUMNO -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionAmbiente/Laboratorios/ReservaPre.svc/ListarPreReservasLabxAlumno"
                URL.PREG_LAB_VERIFICAR_TIPO_POLITICA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionSeguridad/Politicas/PoliticaUsoPortal.svc/VerificarTipoPoliticaPortal"
                URL.PREG_LAB_ACEPTAR_TIPO_POLITICA -> return (if (ReleasePortal) DominioPortal else DominioPortalTest) + "/GestionSeguridad/Politicas/PoliticaUsoPortal.svc/AceptarTipoPolitica"

                URL.MENU_COMEDOR ->                 return "http://intranetmovil.ue.edu.pe/Service/AlumnoService.svc/menuComedor/"
                /*URL.MENU_COMEDOR ->                 return "http://devintranetmovil.ue.edu.pe/Service/AlumnoService.svc/menuComedor/"*/

                //REALIDAD AUMENTADA
                URL.RA_FACULTADES ->                return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/facultades"
                URL.RA_EDIFICIOS ->                 return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/edificios"
                URL.RA_CAFETERIAS ->                return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/cafeterias"
                URL.RA_DEPORTES ->                  return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/deportes"
                URL.RA_BIBLIOTECAS ->               return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/bibliotecas"
                URL.RA_LABORATORIOS ->              return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/laboratorios"
                URL.RA_AUDITORIOS ->                return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/auditorios"
                URL.RA_LIBRERIAS ->                 return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/librerias"
                URL.RA_OFICINAS ->                  return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/oficinas"
                URL.RA_AULAS ->                     return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/aulas"
                URL.RA_VARIOS ->                    return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambientegrupo/varios"
                URL.RA_BYID ->                      return "http://intranetmovil.esan.edu.pe/DataUsuario.svc/ambiente/"
                URL.RA_IMAGEN ->                    return "http://intranetmovil.esan.edu.pe/recursos/img/"
                else -> return ""
            }

        }

        /** Obtener la url de la foto del usuario
         * @param codigo representa el código del alumnos o profesor
         * @param size representa el tamaño de imagen se recomienta valores 140 y 80
         * @return String URL
         */
        fun getUrlFoto(codigo: String, size: Int): String {
            return "http://fotos.ue.edu.pe/Inicio/MostrarFotoActorxCodigo/?codigo=$codigo&width=$size"
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
            return "http://recibos.ue.edu.pe/recibos/$codigo.pdf"
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
            when (type) {
                TypeFont.THIN -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Thin.ttf")
                TypeFont.THIN_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-ThinItalic.ttf")
                TypeFont.LIGHT -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Light.ttf")
                TypeFont.LIGHT_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-LightItalic.ttf")
                TypeFont.REGULAR -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")
                TypeFont.REGULAR_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Italic.ttf")
                TypeFont.MEDIUM -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Medium.ttf")
                TypeFont.MEDIUM_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-MediumItalic.ttf")
                TypeFont.BOLD -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Bold.ttf")
                TypeFont.BOLD_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-BoldItalic.ttf")
                TypeFont.BLACK -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Black.ttf")
                TypeFont.BLACK_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-BlackItalic.ttf")
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
            if (sDia < 0) return 6 else return sDia
        }

        fun getStringToDateHHmm(hora: String) : Date?{
            try {
                val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
                return hhmm.parse(hora)
            } catch (pe: ParseException) {
                return null
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
            calendar.time = fecha
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

        fun getStringToStringddMMyyyyHHmm(fecha: String): String {
            val ddMMyyyyHHmm = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            var f: Date
            try {
                var newfecha = fecha.replace("/Date(", "")
                newfecha = newfecha.replace(")/", "")
                val `val` = newfecha.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val va = java.lang.Long.parseLong(`val`[0])
                f = Date(va)
            } catch (e: Exception) {
                f = Date()
            }

            return ddMMyyyyHHmm.format(f!!.time)
        }

        fun getStringToDateddMMyyyy(fecha: String): Date {
            try {
                var newfecha = fecha.replace("/Date(", "")
                newfecha = newfecha.replace(")/", "")
                val `val` = newfecha.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val va = java.lang.Long.parseLong(`val`[0])
                return Date(va)
            } catch (e: Exception) {
                return Date()
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
            /*val yyyyMMddTHHmm = SimpleDateFormat("yyyy-MM-ddTHH:mm:ss", Locale.getDefault())*/
            var f: Date
            try {
                f = ddMMyyyyHHmm.parse(fecha)
            }catch (e: Exception) {
                f = Date()
            }

            return ddMMyyyyHHmm.format(f.time)
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

        private val characterEncoding = "UTF-8"
        private val cipherTransformation = "AES/CBC/PKCS5Padding"
        private val aesEncryptionAlgorithm = "AES"

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
            }
        }
    } else {
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    return false
}