package villealla.com.arinvaders

class Configuration {

    companion object {

        const val UFO = "CUPIC_FYINGSAUCER"

        const val NUM_OF_SHIPS_IN_WAVE_DEFAULT = 30
        const val SECONDS_BETWEEN_WAVES_SHORT = 30
        const val SECONDS_BETWEEN_WAVES_MEDIUM = 60
        const val SECONDS_BETWEEN_WAVES_LONG = 90

        const val SHIP_MOVE_SPEED = 10

        // ships die in one hit I guess... hard to keep track of hits so probably not worth it
        const val INITIAL_PLANET_HP = 10

        const val MIN_SPAWN_X = 1.0F
        const val MAX_SPAWN_X = 2.0F
        const val MIN_SPAWN_Y = 1.0F
        const val MAX_SPAWN_Y = 2.0F
        const val MIN_SPAWN_Z = 1.0F
        const val MAX_SPAWN_Z = 2.0F

        const val DELAY_MS = 10L // just here to remind that delays have to be in ms (long)
    }
} // end class