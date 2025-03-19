package io.sebi.huemcp

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.sebi.hueclient.HueClient
import io.sebi.hueclient.HueClientImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

fun main() {
    val server: Server = createServer()
    val stdioServerTransport = StdioServerTransport(
        System.`in`.asSource().buffered(),
        System.out.asSink().buffered()
    )
    runBlocking {
        val job = Job()
        server.onCloseCallback = { job.complete() }
        server.connect(stdioServerTransport)
        job.join()
    }
}

fun createServer(): Server {
    val info = Implementation(
        "Hue Office MCP",
        "1.0.0"
    )
    val options = ServerOptions(
        capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(true))
    )
    val server = Server(info, options)
    val hueClient: HueClient = HueClientImpl()
    runBlocking {
        hueClient.authenticate("mcp-hue-app", "/Users/Sebastian.Aigner/Desktop/github/huelight-mcp/credentials.txt")
    }
    server.addTool(
        "list-office-light-ids",
        "Returns the light IDs for the office."
    ) {
        CallToolResult(
            listOf(
                TextContent(hueClient.findLightIdsForRoom("Office").joinToString())
            )
        )
    }
    
    val setLightStateInputSchema = Tool.Input(
        buildJsonObject {
            put("lightId", "string")
            put("on", "boolean")
            put("brightness", "number")
            put("colorGamutX", "number")
            put("colorGamutY", "number")
            put("dimming", "number")
        }
    )
    
    server.addTool(
        "set-light-state",
        "Sets the light state for a given light. Brightness goes from 0 to 100.",
        setLightStateInputSchema
    ) { input ->
        val id = input.arguments["lightId"]!!.jsonPrimitive.content
        val on = input.arguments["on"]?.jsonPrimitive?.booleanOrNull
        val brightness = input.arguments["brightness"]?.jsonPrimitive?.doubleOrNull
        val colorGamutX = input.arguments["colorGamutX"]?.jsonPrimitive?.doubleOrNull
        val colorGamutY = input.arguments["colorGamutY"]?.jsonPrimitive?.doubleOrNull
        val dimming = input.arguments["dimming"]?.jsonPrimitive?.doubleOrNull
        hueClient.setLightState(id, on, brightness, colorGamutX, colorGamutY, dimming)
        CallToolResult(
            listOf(
                TextContent("Done!")
            )
        )
    }

    return server
}
