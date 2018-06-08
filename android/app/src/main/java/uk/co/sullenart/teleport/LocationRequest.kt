package uk.co.sullenart.teleport

data class LocationRequest(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
) {
    override fun toString(): String = String.format("%f, %f", latitude, longitude)
}
