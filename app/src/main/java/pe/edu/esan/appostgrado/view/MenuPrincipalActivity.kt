package pe.edu.esan.appostgrado.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_menu_principal.*
import kotlinx.android.synthetic.main.toolbar_menuprincipal.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.analytics.AnalyticsApplication
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.Profesor
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.helpers.ShowAlertHelper
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.academico.docente.SeccionesFragment
import pe.edu.esan.appostgrado.view.academico.postgrado.ProgramasFragment
import pe.edu.esan.appostgrado.view.academico.pregrado.CursosFragment
import pe.edu.esan.appostgrado.view.horario.HorarioFragment
import pe.edu.esan.appostgrado.view.mas.MasFragment
import pe.edu.esan.appostgrado.view.pago.PagoPostFragment
import pe.edu.esan.appostgrado.view.pago.PagoPreFragment
import pe.edu.esan.appostgrado.view.puntosreunion.postgrado.PuntosReunionPosgradoFragment
import pe.edu.esan.appostgrado.view.puntosreunion.pregrado.PuntosReunionPregradoFragment
import java.util.*

class MenuPrincipalActivity : AppCompatActivity() {

    private val TAB_HORARIO = R.string.m_tab_horario
    private val TAB_CURSOS = R.string.m_tab_cursos
    private val TAB_PROGRAMAS = R.string.m_tab_programas
    private val TAB_SECCIONES = R.string.m_tab_secciones
    private val TAB_PAGOS = R.string.m_tab_pagos

    private val TAB_PUNTOSREUNION = R.string.m_tab_puntosreunion
    private val TAB_SALAS_ESTUDIO = R.string.m_tab_salas_estudio
    private val TAB_MAS = R.string.m_tab_mas

    private val T_HORARIO = R.string.m_t_horario
    private val T_CURSOS = R.string.m_t_cursos
    private val T_PROGRAMAS = R.string.m_t_programas
    private val T_SECCIONES = R.string.m_t_secciones
    private val T_PAGOS = R.string.m_t_pagos

    private val T_PUNTOSREUNION = R.string.m_t_puntosreunion
    private val T_SALAS_ESTUDIO = R.string.m_t_salas_estudio
    private val T_MAS = R.string.m_t_mas

    var toolbar : Toolbar? = null
    var lblTituloActivity : TextView? = null
    var mTracker : Tracker? = null
    var tipoUsuario = ""

    private val LOG = MenuPrincipalActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        val application = application as AnalyticsApplication
        mTracker = application.getDefaultTracker()

        toolbar = main_toolbar as Toolbar
        toolbar?.toolbar_title?.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        lblTituloActivity = toolbar?.toolbar_title

        setSupportActionBar(toolbar)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)
        controlViewModel.refreshDataForFragmentPublic.observe(this,
            Observer<Boolean> { value ->
                if(value){
                    refreshDataForFragment()
                }
            })

        if (ControlUsuario.instance.currentUsuario.size != 0  && ControlUsuario.instance.currentUsuarioGeneral != null) {
            controlViewModel.insertDataToRoom()
            mainSetup()
        } else {
            if(intent.hasExtra("login_invitado")){
                if(intent.getStringExtra("login_invitado") == "es_invitado"){
                    mainSetup()
                }
                intent.removeExtra("login_invitado")
            } else {
                controlViewModel.dataIsReadyPublic.observe(this,
                    Observer<Boolean> { value ->
                        if(value){
                            mainSetup()
                            controlViewModel.setValueForDataIsReady(false)
                        }
                    })

                if(intent.hasExtra("back_from_pregrado_prereserva_lab") || intent.hasExtra("back_from_pregrado_prereserva")){
                    controlViewModel.getDataFromRoom()
                }
            }

        }

    }


    private fun refreshDataForFragment(){
        controlViewModel.getDataFromRoom()
    }


    private fun mainSetup(){

        setUpViewPager(viewPager, ControlUsuario.instance.currentUsuario[0])

        tabs.setupWithViewPager(viewPager)

        tabs.addOnTabSelectedListener( object : TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)

                lblTituloActivity!!.text = tab!!.text.toString()

                if (tab.text.toString() != resources.getString(TAB_MAS)){
                    println(tab.text.toString())
                    sendAnalyticsOption(tipoUsuario, tab.text.toString())
                }
            }
        })
        tabs.requestLayout()
        setUpTabIcons(ControlUsuario.instance.currentUsuario[0])

        checkTabSelection()

        //SUBSCRIBE USER TO TOPICS
        /*subscribeUserToTopics()*/

        if(intent.hasExtra("back_from_pregrado_prereserva")){

            val isReturning = intent.getIntExtra("back_from_pregrado_prereserva", -1) == 0

            if (isReturning){
                tabs.getTabAt(3)?.select()
            }

            intent.removeExtra("back_from_pregrado_prereserva")
        } else if(intent.hasExtra("back_from_pregrado_prereserva_lab")){

            val isReturning = intent.getIntExtra("back_from_pregrado_prereserva_lab", -1) == 0

            if (isReturning){
                tabs.getTabAt(4)?.select()
                if(intent.hasExtra("tyc_rechazo")){
                    if(intent.getBooleanExtra("tyc_rechazo", false)) {
                        val snack = Snackbar.make(findViewById(android.R.id.content),
                            "Debe aceptar los términos y condiciones para utilizar los laboratorios",
                            Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            .setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                        snack.show()
                    }
                }
            }
            intent.removeExtra("back_from_pregrado_prereserva_lab")
        }
    }

    private fun checkTabSelection(){
        if(tabs.tabCount > 1){
            for (index in 0 until tabs.tabCount){
                val tab = tabs.getTabAt(index)
                if (tab != null) {
                    if(tab.isSelected){
                        lblTituloActivity!!.text = tab.text.toString()
                    }
                }
            }
        }

    }


    private fun setUpViewPager(viewPager: androidx.viewpager.widget.ViewPager, usuario: UserEsan) {

        val adapter = ViewPagerAdapter(supportFragmentManager)
        when (usuario) {
            is Alumno -> {
                println("ES ALUMNO")
                if (usuario.tipoAlumno.equals(Utilitarios.POS)) {
                    println("ALUMNO POS")
                    lblTituloActivity!!.text = resources.getString(TAB_HORARIO)
                    adapter.addFragment(HorarioFragment(), resources.getString(TAB_HORARIO))
                    adapter.addFragment(ProgramasFragment(), resources.getString(TAB_PROGRAMAS))
                    adapter.addFragment(PagoPostFragment(), resources.getString(TAB_PAGOS))
                    adapter.addFragment(PuntosReunionPosgradoFragment(), resources.getString(TAB_PUNTOSREUNION))
                    adapter.addFragment(MasFragment(), resources.getString(TAB_MAS))
                } else {
                    println("ALUMNO PRE")
                    lblTituloActivity!!.text = resources.getString(TAB_HORARIO)
                    adapter.addFragment(HorarioFragment(), resources.getString(TAB_HORARIO))
                    adapter.addFragment(CursosFragment(), resources.getString(TAB_CURSOS))
                    adapter.addFragment(PagoPreFragment(), resources.getString(TAB_PAGOS))
                    adapter.addFragment(PuntosReunionPregradoFragment(), resources.getString(TAB_SALAS_ESTUDIO))
                    adapter.addFragment(MasFragment(), resources.getString(TAB_MAS))
                }
            }
            is Profesor -> {
                println("ES PROFESOR")
                lblTituloActivity!!.text = resources.getString(TAB_HORARIO)
                adapter.addFragment(HorarioFragment(), resources.getString(TAB_HORARIO))
                adapter.addFragment(SeccionesFragment(), resources.getString(TAB_SECCIONES))
                adapter.addFragment(MasFragment(), resources.getString(TAB_MAS))
            }
            else -> {
                println("INVITADO")
                lblTituloActivity!!.text = resources.getString(TAB_MAS)
                adapter.addFragment(MasFragment(), resources.getString(TAB_MAS))
            }
        }
        viewPager.adapter = adapter

    }


    private fun sendAnalyticsOption(actor: String, opcion: String) {
        mTracker?.send(HitBuilders.EventBuilder()
            .setCategory(actor)
            .setAction(opcion)
            .build())
    }


    private fun setUpTabIcons(usuario: UserEsan) {

        when (usuario) {
            is Alumno -> {
                println("ES ALUMNO")
                if (usuario.tipoAlumno.equals(Utilitarios.POS)) {
                    println("ALUMNO POS")
                    tipoUsuario = "AlumnoPosgrado"

                    val poshorario= LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    poshorario.text = resources.getString(T_HORARIO)
                    poshorario.isSelected = true
                    poshorario.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_horario, 0, 0)
                    tabs.getTabAt(0)?.customView = (poshorario)

                    val posprogramas = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    posprogramas.text = resources.getString(T_PROGRAMAS)
                    posprogramas.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_curso, 0, 0)
                    tabs.getTabAt(1)?.customView = (posprogramas)

                    val pospagos = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    pospagos.text = resources.getString(T_PAGOS)
                    pospagos.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_pagos, 0, 0)
                    tabs.getTabAt(2)!!.customView = (pospagos)

                    val pospp = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    pospp.text = resources.getString(T_PUNTOSREUNION)
                    pospp.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_puntoreunion, 0, 0)
                    tabs.getTabAt(3)!!.customView = (pospp)

                    val posmas = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    posmas.text = resources.getString(T_MAS)
                    posmas.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_mas, 0, 0)
                    tabs.getTabAt(4)!!.customView = (posmas)

                } else {
                    println("ALUMNO PRE")
                    tipoUsuario = "AlumnoPregrado"
                    val poshorario = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    poshorario.text = resources.getString(T_HORARIO)
                    poshorario.isSelected = true
                    poshorario.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_horario, 0, 0)
                    tabs.getTabAt(0)?.customView = (poshorario)

                    val precursos = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    precursos.text = resources.getString(T_CURSOS)
                    precursos.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_curso, 0, 0)
                    tabs.getTabAt(1)?.customView = (precursos)

                    val prepagos = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    prepagos.text = resources.getString(T_PAGOS)
                    prepagos.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_pagos, 0, 0)
                    tabs.getTabAt(2)?.customView = (prepagos)

                    val prepp = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    prepp.text = resources.getString(T_SALAS_ESTUDIO)
                    prepp.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_puntoreunion, 0, 0)
                    tabs.getTabAt(3)?.customView = (prepp)

                    val premas = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                    premas.text = resources.getString(T_MAS)
                    premas.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_mas, 0, 0)
                    tabs.getTabAt(4)?.customView = (premas)

                }
            }
            is Profesor -> {
                println("ES PROFESOR")
                tipoUsuario = "Profesor"
                val poshorario = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                poshorario.text = resources.getString(T_HORARIO)
                poshorario.isSelected = true
                poshorario.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_horario, 0, 0)
                tabs.getTabAt(0)?.customView = poshorario

                val profseccion = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                profseccion.text = resources.getString(T_SECCIONES)
                profseccion.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_curso, 0, 0)
                tabs.getTabAt(1)?.customView = profseccion

                val profmas = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                profmas.text = resources.getString(T_MAS)
                profmas.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_mas, 0, 0)
                tabs.getTabAt(2)?.customView = profmas
            }
            else -> {
                println("INVITADO")
                tipoUsuario = "Invitado"

                val invmas = LayoutInflater.from(this).inflate(R.layout.tab_menuprincipal, tabs, false) as TextView
                invmas.text = resources.getString(T_MAS)
                invmas.isSelected = true
                invmas.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_mas, 0, 0)
                tabs.getTabAt(0)!!.customView = invmas
            }
        }
    }



    @SuppressLint("WrongConstant")
    internal inner class ViewPagerAdapter(manager: androidx.fragment.app.FragmentManager): FragmentStatePagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        if (ControlUsuario.instance.currentUsuario.size != 0  && ControlUsuario.instance.currentUsuarioGeneral != null) {
            controlViewModel.insertDataToRoom()
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_menuprincipal, menu)
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (ControlUsuario.instance.currentUsuario.size == 1) {
                val usuario = ControlUsuario.instance.currentUsuario[0]
                when (usuario) {
                    is Alumno -> {
                        if (usuario.tipoAlumno.equals(Utilitarios.POS)) {
                            //ALUMNO POSTGRADO
                            tabs.getTabAt(4)?.select()
                        } else {
                            //ALUMNO PREGRADO
                            tabs.getTabAt(4)?.select()
                        }
                    }
                    is Profesor -> {
                        tabs.getTabAt(2)?.select()
                    }
                    else -> {
                        tabs.getTabAt(0)?.select()
                    }
                }
                return true
            } else {
                if(tipoUsuario != "Invitado"){
                    val showAlertHelper = ShowAlertHelper(this)
                    showAlertHelper.showAlertError(
                        resources.getString(R.string.error),
                        resources.getString(R.string.error_ingreso)
                    ) {  android.os.Process.killProcess(android.os.Process.myPid()) }
                } else {
                    tabs.getTabAt(0)?.select()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }



    //****************************************************************** TOPICS ************************************************************************************
    //SUBSCRIBE TO TOPICS
    /*private fun subscribeUserToTopics(){
        val user = ControlUsuario.instance.currentUsuario[0]

        when(user){
            is Alumno -> {
                if(user.tipoAlumno == Utilitarios.PRE){
                    executeSubscriptionToTopic("notific_alumnos_pregrado")
                } else {
                    executeSubscriptionToTopic("notific_alumnos_postgrado")
                }
                executeSubscriptionToTopic("notific_alumnos")
            }
            is Profesor -> {
                executeSubscriptionToTopic("notific_docentes")
            }
            else -> {
                unsubscribeUserFromTopics()
            }
        }
    }

    //UNSUBSCRIBE FROM TOPICS
    private fun unsubscribeUserFromTopics(){
        executeUnsubscriptionFromTopic("notific_alumnos_pregrado")
        executeUnsubscriptionFromTopic("notific_alumnos_postgrado")
        executeUnsubscriptionFromTopic("notific_alumnos")
        executeUnsubscriptionFromTopic("notific_docentes")
    }

    //SUBSCRIBE USER TO TOPIC
    fun executeSubscriptionToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic $topic"
                if (!task.isSuccessful) {
                    msg = "Error during subscription"
                }
            }
    }

    //UNSUBSCRIBE USER FROM TOPIC
    fun executeUnsubscriptionFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Unsubscribed from topic $topic"
                if (!task.isSuccessful) {
                    msg = "Error during unsubscription"
                }
            }
    }*/


}
