package villealla.com.arinvaders.API

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntry(val username: String, val difficulty: String, val score: Int, @PrimaryKey(autoGenerate = true) val id: Int)