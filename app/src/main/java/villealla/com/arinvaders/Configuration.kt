package villealla.com.arinvaders

// global config objects are supposedly a bad pattern...
// I already moved some stuff out of here; we can probably get
// rid of this class entirely
class Configuration {

    companion object {

        const val SECONDS_BETWEEN_WAVES_SHORT = 30
        const val SECONDS_BETWEEN_WAVES_MEDIUM = 60
        const val SECONDS_BETWEEN_WAVES_LONG = 90

        const val MIN_SPAWN_X = 1.0F
        const val MAX_SPAWN_X = 2.0F
        const val MIN_SPAWN_Y = 1.0F
        const val MAX_SPAWN_Y = 2.0F
        const val MIN_SPAWN_Z = 1.0F
        const val MAX_SPAWN_Z = 2.0F

        const val DELAY_MS = 10L // just here to remind that delays have to be in ms (long)
    }
} // end class