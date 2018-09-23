package villealla.com.arinvaders

import android.content.Context

/*
* Class for managing the player and game-related operations.
*
* */

class GameManager private constructor() {

    init {
        // do stuff when GameManager.instance is assigned
    }

    private object Holder { val INSTANCE = GameManager() }

    // I don't claim to understand this, but it makes this class a Singleton
    companion object {
        val instance: GameManager by lazy { Holder.INSTANCE }
    }

    // the amount of destroyed ships in this game session
    var score = 0

} // end class