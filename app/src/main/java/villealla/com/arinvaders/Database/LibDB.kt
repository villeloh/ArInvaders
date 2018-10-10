package villealla.com.arinvaders.Database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import villealla.com.arinvaders.API.ScoreEntry

@Database(entities = [(ScoreEntry::class)], version = 1)
abstract class LibDB : RoomDatabase() {

    abstract fun scoreEntryDAO(): ScoreEntryDAO


    companion object {
        private var sInstance: LibDB? = null
        @Synchronized
        fun get(context: Context): LibDB {
            if (sInstance == null) {
                sInstance = Room.databaseBuilder(context.applicationContext,
                        LibDB::class.java, "lib.db").build()
            }
            return sInstance!!
        }
    }
}