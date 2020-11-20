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
}