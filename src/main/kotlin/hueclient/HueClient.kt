package io.sebi.hueclient

interface HueClient {
    suspend fun authenticate(appName: String, credentialsPath: String)
    suspend fun findLightIdsForRoom(roomName: String): List<String>
    suspend fun setLightState(
        lightId: String,
        on: Boolean? = null,
        brightness: Double?,
        colorGamutX: Double?,
        colorGamutY: Double?,
        dimming: Double?
    )
}