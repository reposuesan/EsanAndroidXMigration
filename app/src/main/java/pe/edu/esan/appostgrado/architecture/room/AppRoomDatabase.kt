package pe.edu.esan.appostgrado.architecture.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import pe.edu.esan.appostgrado.architecture.typeconverters.ConvertersKotlin

@Database(entities = arrayOf(ControlRoomEntity::class), version = 1,exportSchema = false)
@TypeConverters(ConvertersKotlin::class)
abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun controlRoomDao(): ControlRoomDao

    companion object {
        //Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {

            val tempInstance =
                INSTANCE

            if(tempInstance != null){
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "esan_database"
                    ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

    /*companion object {
        //Singleton prevents multiple instances of database opening at the
        //same time.
        @Volatile
        private var instance: AppRoomDatabase? = null

        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        //TODO: REMOVE MAIN THREAD QUERIES FOR ROOM, USE EXECUTORS OR SOMETHING ELSE
        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            AppRoomDatabase::class.java, "room_database")
            .build()
    }*/
}