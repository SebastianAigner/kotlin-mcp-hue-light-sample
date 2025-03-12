package io.sebi.hueclient

import kotlinx.coroutines.runBlocking
import kotlin.test.*

class HueClientIntegrationTest {
    private lateinit var client: HueClient
    
    @BeforeTest
    fun setup() {
        client = HueClientImpl()
    }
    
    @Test
    fun `test office lights control`() = runBlocking {
        // First authenticate
        client.authenticate("test-app", "credentials.txt")
        
        // Find lights in the office
        val officeLightIds = client.findLightIdsForRoom("Office")
        println("[DEBUG_LOG] Found ${officeLightIds.size} lights in office: $officeLightIds")
        assertNotEquals(0, officeLightIds.size, "Should find lights in the office")
        
        // Test turning all lights off
        officeLightIds.forEach { lightId ->
            client.setLightState(lightId, on = false, brightness = null, colorGamutX = null, colorGamutY = null, dimming = null)
        }
        println("[DEBUG_LOG] Turned all lights off")
        Thread.sleep(2000) // Wait to see the effect
        
        // Test turning all lights on with 50% brightness
        officeLightIds.forEach { lightId ->
            client.setLightState(lightId, on = true, brightness = 50.0, colorGamutX = null, colorGamutY = null, dimming = null)
        }
        println("[DEBUG_LOG] Turned all lights on at 50% brightness")
        Thread.sleep(2000) // Wait to see the effect
        
        // Test changing color to purple (using values from the documentation)
        officeLightIds.forEach { lightId ->
            client.setLightState(lightId, on = true, brightness = null, colorGamutX = 0.4605, colorGamutY = 0.2255, dimming = null)
        }
        println("[DEBUG_LOG] Changed lights to purple")
        Thread.sleep(2000) // Wait to see the effect
        
        // Return to normal white light at full brightness
        officeLightIds.forEach { lightId ->
            client.setLightState(lightId, on = true, brightness = 100.0, colorGamutX = 0.3127, colorGamutY = 0.3290, dimming = null)
        }
        println("[DEBUG_LOG] Returned lights to normal white at full brightness")
    }
}