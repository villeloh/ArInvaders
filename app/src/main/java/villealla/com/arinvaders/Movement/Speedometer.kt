package villealla.com.arinvaders.Movement

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/*
* Manages the ship's speedometer (it's just a cosmetic feature,
* showing speed values based on the phone's acceleration).
* @author Ville Lohkovuori
* */

class Speedometer(private val sensorManager: SensorManager, private val activity: SpeedometerListener): SensorEventListener {

    private var ownShipSpeed: Float = 0f
    private var accX: Float = 0f
    private var accY: Float = 0f
    private var accZ: Float = 0f
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    // used for communicating with MainActivity
    interface SpeedometerListener {

        fun onSpeedChange(ownShipSpeed: Float)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event == null) return

        // to limit the update speed of the speedometer to a human-readable level
        val timeCondition = (event.timestamp / 100000000L) % 5 == 0L

        if (event.sensor == sensor && timeCondition) {

            accX = event.values?.get(0) ?: 0f
            accY = event.values?.get(1) ?: 0f
            accZ = event.values?.get(2) ?: 0f
            val exp = 2.0
            val accXSquared = Math.pow(accX.toDouble(),  exp).toFloat()
            val accYSquared = Math.pow(accY.toDouble(),  exp).toFloat()
            val accZSquared = Math.pow(accZ.toDouble(),  exp).toFloat()
            ownShipSpeed = sqrt(accXSquared + accYSquared + accZSquared) * 1000 // gives believable 'space speeds'

            activity.onSpeedChange(ownShipSpeed)
        } // end if
    } // end onSensorChanged

    // it needs to exist whether needed or not
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    fun onActivityPause() {
        sensorManager.unregisterListener(this)
    }

    fun onActivityResume() {

        // for some reason, this needs to be done again with every resume
        sensor.also {
            sensorManager.registerListener(this, it,
                    SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

} // end class