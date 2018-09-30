package villealla.com.arinvaders.Static

// global config objects are supposedly a bad pattern...
// I already moved some stuff out of here; we can probably get
// rid of this class entirely
class Configuration {

    companion object {

        const val SECONDS_BETWEEN_WAVES_SHORT = 30
        const val SECONDS_BETWEEN_WAVES_MEDIUM = 60
        const val SECONDS_BETWEEN_WAVES_LONG = 90

        const val DEBUG_TAG = "MYAPP"

        const val DEFAULT_UNSCALED_MIN_DMG = 100000000L
        const val DEFAULT_UNSCALED_MAX_DMG = 200000000L
    }
} // end class