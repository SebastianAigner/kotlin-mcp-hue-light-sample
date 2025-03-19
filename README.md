# 🎥 Building MCP with Kotlin: Claude Desktop Controls my Smart Home Lights!

_This repo contains supplemental code for [this YouTube video](https://youtu.be/jwevCa96Z4Q)._

The Model Context Protocol Kotlin SDK (https://github.com/modelcontextprotocol/kotlin-sdk) makes it easy for you to build integrations between LLM clients like Claude Desktop and your own applications, whether that's software or hardware! Let's see it in practice: Join Sebastian Aigner building an integration between Hue Smart Lights and Claude Desktop, powered entirely by Kotlin. We walk through defining our interfaces, delegating some of the more mechanical tasks when working with Ktor to Junie, and then implement a basic version of an MCP using the official SDK for Kotlin. We use the MCP inspector to explore and debug our application, and finally integrate the Kotlin Gradle application with Claude Desktop by setting up the final configuration. In the end, we have a self-sufficient MCP server that can control smart lights with natural language!

Junie, the coding agent by JetBrains: https://www.jetbrains.com/junie/

MCP Server Plugin for JetBrains IDEs: https://plugins.jetbrains.com/plugin/26071-mcp-server

MCP inspector: https://github.com/modelcontextprotocol/inspector
