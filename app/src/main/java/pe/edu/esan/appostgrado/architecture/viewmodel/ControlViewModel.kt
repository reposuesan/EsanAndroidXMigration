package pe.edu.esan.appostgrado.architecture.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pe.edu.esan.appostgrado.architecture.room.AppRoomDatabase
import pe.edu.esan.appostgrado.architecture.room.ControlRoomEntity
import pe.edu.esan.appostgrado.architecture.room.ControlRoomDao
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.Profesor
import java.util.concurrent.atomic.AtomicBoolean

class ControlViewModel(application: Application) : AndroidViewModel(application) {

    private val LOG = ControlViewModel::class.simpleName

    private val controlDao: ControlRoomDao = AppRoomDatabase.getDatabase(application).controlRoomDao()

    private var isDatabaseOperationOngoing = AtomicBoolean(false)

    private val dataIsReadyPrivate = MutableLiveData<Boolean>(false)
    private val refreshDataForFragmentPrivate = MutableLiveData<Boolean>(false)
    private val dataWasRetrievedForFragmentPrivate = MutableLiveData<Boolean>(false)
    private val dataWasRetrievedForActivityPrivate = MutableLiveData<Boolean>(false)
    private val proceedLogoutPrivate = MutableLiveData<Boolean>(false)

    val dataWasRetrievedForFragmentPublic: LiveData<Boolean>
        get() = dataWasRetrievedForFragmentPrivate

    val dataWasRetrievedForActivityPublic: LiveData<Boolean>
        get() = dataWasRetrievedForActivityPrivate

    val dataIsReadyPublic: LiveData<Boolean>
        get() = dataIsReadyPrivate

    val refreshDataForFragmentPublic: LiveData<Boolean>
        get() = refreshDataForFragmentPrivate

    val proceedLogoutPublic: LiveData<Boolean>
        get() = proceedLogoutPrivate

    fun deleteDataFromRoom() = viewModelScope.launch(Dispatchers.IO){

        val operation = async {
            controlDao.deleteControlRoom()

            val rowsNumber = controlDao.getRowCount()
        }

        operation.await()

        proceedWithLogout(true)

    }


    fun insertDataToRoom() = viewModelScope.launch(Dispatchers.IO) {
        insertData()
    }

    suspend fun insertData(){
        if(ControlUsuario.instance.currentUsuario.size != 0) {
            val temp = ControlUsuario.instance.currentUsuario[0]
            var type = ""
            if (temp is Alumno) {
                type = "Alumno"
            } else if (temp is Profesor) {
                type = "Profesor"
            } else {
                type = "Invitado"
            }

            val input = ControlRoomEntity(
                0,
                ControlUsuario.instance.currentUsuarioGeneral,
                ControlUsuario.instance.statusLogout,
                ControlUsuario.instance.currentUsuario,
                ControlUsuario.instance.entroEncuesta,
                ControlUsuario.instance.currentListHorario,
                ControlUsuario.instance.currentListHorarioSelect,
                ControlUsuario.instance.copiarListHorario,
                ControlUsuario.instance.currentHorario,
                ControlUsuario.instance.accesoCamara,
                ControlUsuario.instance.accesoGPS,
                ControlUsuario.instance.prconfiguracion,
                ControlUsuario.instance.prPromocionConfig,
                ControlUsuario.instance.prreserva,
                ControlUsuario.instance.esReservaExitosa,
                ControlUsuario.instance.creoMomificoGrupo,
                ControlUsuario.instance.currentMiGrupo,
                ControlUsuario.instance.cambioPantalla,
                ControlUsuario.instance.pantallaSuspendida,
                ControlUsuario.instance.recargaHorarioProfesor,
                ControlUsuario.instance.tomoAsistenciaMasica,
                ControlUsuario.instance.indexActualiza,
                ControlUsuario.instance.currentCursoPost,
                ControlUsuario.instance.currentCursoPre,
                ControlUsuario.instance.currentSeccion,
                type
            )

            controlDao.insertAll(input)

            val rowsNumber = controlDao.getRowCount()
        }
    }


    fun getDataFromRoom() = viewModelScope.launch(Dispatchers.IO) {

        if(!isDatabaseOperationOngoing.get()) {

            isDatabaseOperationOngoing.set(true)

            val operation = async {
                val list = controlDao.getAll()

                if(list.isNotEmpty()) {

                    val item = list[0]

                    ControlUsuario.instance.currentUsuarioGeneral = item.currentUsuarioGeneral
                    ControlUsuario.instance.statusLogout = item.statusLogout
                    ControlUsuario.instance.currentUsuario.clear()

                    if (item.type.equals("Alumno")) {
                        ControlUsuario.instance.currentUsuario.add(item.currentUsuario[0] as Alumno)
                    } else if (item.type.equals("Profesor")) {
                        ControlUsuario.instance.currentUsuario.add(item.currentUsuario[0] as Profesor)
                    } else {
                        ControlUsuario.instance.currentUsuario.add(item.currentUsuario[0])
                    }


                    ControlUsuario.instance.entroEncuesta = item.entroEncuesta
                    ControlUsuario.instance.currentListHorario = item.currentListHorario

                    ControlUsuario.instance.currentListHorarioSelect = item.currentListHorarioSelect
                    ControlUsuario.instance.copiarListHorario = item.copiarListHorario
                    ControlUsuario.instance.currentHorario = item.currentHorario
                    ControlUsuario.instance.accesoCamara = item.accesoCamara
                    ControlUsuario.instance.accesoGPS = item.accesoGPS
                    ControlUsuario.instance.prconfiguracion = item.prconfiguracion
                    ControlUsuario.instance.prPromocionConfig = item.prPromocionConfig
                    ControlUsuario.instance.prreserva = item.prreserva
                    ControlUsuario.instance.esReservaExitosa = item.esReservaExitosa

                    ControlUsuario.instance.creoMomificoGrupo = item.creoMomificoGrupo
                    ControlUsuario.instance.currentMiGrupo = item.currentMiGrupo
                    ControlUsuario.instance.cambioPantalla = item.cambioPantalla
                    ControlUsuario.instance.pantallaSuspendida = item.pantallaSuspendida
                    ControlUsuario.instance.recargaHorarioProfesor = item.recargaHorarioProfesor
                    ControlUsuario.instance.tomoAsistenciaMasica = item.tomoAsistenciaMasica
                    ControlUsuario.instance.indexActualiza = item.indexActualiza

                    ControlUsuario.instance.currentCursoPost = item.currentCursoPost
                    ControlUsuario.instance.currentCursoPre = item.currentCursoPre
                    ControlUsuario.instance.currentSeccion = item.currentSeccion
                }
            }

            operation.await()
            informUICanBeRendered()
        }
    }

    private fun informUICanBeRendered(){

        isDatabaseOperationOngoing.set(false)

        dataIsReadyPrivate.postValue(true)

        databaseRetrievalFinished(true)

        resetRefreshConditionInBackground(false)
    }

    fun setValueForDataIsReady(status: Boolean){
        dataIsReadyPrivate.value = status
    }

    fun refreshDataForFragment(status: Boolean){
        refreshDataForFragmentPrivate.value = status
    }

    private fun resetRefreshConditionInBackground(status: Boolean){
        refreshDataForFragmentPrivate.postValue(status)
    }

    private fun databaseRetrievalFinished(status: Boolean){
        dataWasRetrievedForFragmentPrivate.postValue(status)
        dataWasRetrievedForActivityPrivate.postValue(status)
    }

    private fun proceedWithLogout(status: Boolean){
        proceedLogoutPrivate.postValue(status)
    }

}