package villealla.com.arinvaders

import villealla.com.arinvaders.Configuration as c

class Ship(val type: String = c.UFO, val speed: Int = c.SHIP_MOVE_SPEED) {

    // ships are pretty bare-bones atm, but could get more complex with time,
    // so I made them their own class
}