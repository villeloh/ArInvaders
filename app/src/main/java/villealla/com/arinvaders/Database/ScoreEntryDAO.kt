package villealla.com.arinvaders.Database

import android.arch.persistence.room.*
import villealla.com.arinvaders.API.ScoreEntry

@Dao
interface ScoreEntryDAO {

    @Query("SELECT * FROM scores WHERE username= :username AND difficulty= :difficulty ORDER BY score DESC LIMIT 8")
    fun getTopScoresWithDifficulty(username: String, difficulty: String): List<ScoreEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(scoreEntry: ScoreEntry)

    @Update
    fun update(scoreEntry: ScoreEntry)

}