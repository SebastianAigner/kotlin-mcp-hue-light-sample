package io.sebi.hueclient

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.io.File
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class HueClientImpl : HueClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            }
        }
    }

    private val bridgeUrl = "https://192.168.178.162"
    private var applicationKey: String? = null

    override suspend fun authenticate(appName: String, credentialsPath: String) {
        applicationKey = File(credentialsPath).readText().trim()
    }

    override suspend fun findLightIdsForRoom(roomName: String): List<String> {
        requireNotNull(applicationKey) { "Must authenticate first" }

        // First, find the room
        val rooms = client.get("$bridgeUrl/clip/v2/resource/room") {
            headers {
                append("hue-application-key", applicationKey!!)
            }
        }.bodyAsText()


        val roomsJson = Json.parseToJsonElement(rooms).jsonObject
        val roomData = roomsJson["data"]?.jsonArray ?: return emptyList()


        val targetRoom = roomData.find { 
            it.jsonObject["metadata"]?.jsonObject?.get("name")?.jsonPrimitive?.content == roomName 
        }

        if (targetRoom == null) {
            return emptyList()
        }

        // Get all lights in the room
        val roomId = targetRoom.jsonObject["id"]?.jsonPrimitive?.content ?: return emptyList()

        val lights = client.get("$bridgeUrl/clip/v2/resource/light") {
            headers {
                append("hue-application-key", applicationKey!!)
            }
        }.bodyAsText()


        val lightsJson = Json.parseToJsonElement(lights).jsonObject
        val lightData = lightsJson["data"]?.jsonArray ?: return emptyList()

        // Get all device IDs in the room
        val deviceIds = targetRoom.jsonObject["children"]?.jsonArray
            ?.filter { it.jsonObject["rtype"]?.jsonPrimitive?.content == "device" }
            ?.mapNotNull { it.jsonObject["rid"]?.jsonPrimitive?.content }
            ?: emptyList()


        val result = lightData.mapNotNull { light ->
            val owner = light.jsonObject["owner"]?.jsonObject?.get("rid")?.jsonPrimitive?.content
            if (deviceIds.contains(owner)) {
                light.jsonObject["id"]?.jsonPrimitive?.content
            } else null
        }

        return result
    }

    override suspend fun setLightState(
        lightId: String,
        on: Boolean?,
        brightness: Double?,
        colorGamutX: Double?,
        colorGamutY: Double?,
        dimming: Double?
    ) {
        requireNotNull(applicationKey) { "Must authenticate first" }

        val requestBody = buildJsonObject {
            on?.let { put("on", buildJsonObject { put("on", it) }) }
            if (colorGamutX != null && colorGamutY != null) {
                put("color", buildJsonObject {
                    put("xy", buildJsonObject {
                        put("x", colorGamutX)
                        put("y", colorGamutY)
                    })
                })
            }
            brightness?.let { 
                put("dimming", buildJsonObject { put("brightness", it) })
            }
            dimming?.let {
                put("dimming", buildJsonObject { put("brightness", it) })
            }
        }

        client.put("$bridgeUrl/clip/v2/resource/light/$lightId") {
            headers {
                append("hue-application-key", applicationKey!!)
            }
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }
    }
}
