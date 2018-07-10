package uk.co.sullenart.teleport.model

data class LocationRequest(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
) {
    companion object {
        fun createInvalid() = LocationRequest()
    }

    fun isValid() = (latitude != 0.0 && longitude != 0.0)

    override fun toString(): String = String.format("%f, %f", latitude, longitude)
}
