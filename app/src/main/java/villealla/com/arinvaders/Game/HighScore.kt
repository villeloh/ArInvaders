package villealla.com.arinvaders.Game

/*
* A class for structuring player high scores, for showing them
* in our high score list views.
* @author Ville Lohkovuori
* */

class HighScore(private val listIndex: Int, private val playerName: String, private val score: Int) {

    override fun toString(): String {

        return "$listIndex. $playerName: $score"
    }
}