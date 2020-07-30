package pe.edu.esan.appostgrado.architecture.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.entidades.UsuarioGeneral

@Dao
interface ControlRoomDao {

    @Query("SELECT * FROM control_room")
    fun getAll(): List<ControlRoomEntity>

    //@Query("SELECT currentUsuario FROM control_room")
    //fun loadCurrentUsuario(): ArrayList<Any>

    @Query("SELECT currentUsuarioGeneral FROM control_room")
    fun loadCurrentUsuarioGeneral(): UsuarioGeneral?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg controlRoomEntity: ControlRoomEntity)

    @Query("UPDATE control_room SET currentUsuario = :currentUsuario WHERE id = :tid")
    fun updateCurrentUsuario(tid: Long, currentUsuario: ArrayList<UserEsan>)

    @Query("UPDATE control_room SET currentUsuarioGeneral = :currentUsuarioGeneral WHERE id = :tid")
    fun updateCurrentUsuarioGeneral(tid: Long, currentUsuarioGeneral: UsuarioGeneral)

    @Query("DELETE FROM control_room WHERE id = :tid")
    fun deleteControlRoomUsingId(tid: Long)

    @Query("DELETE FROM control_room")
    fun deleteControlRoom()

    @Query("SELECT COUNT(id) FROM control_room")
    fun getRowCount(): Int
}