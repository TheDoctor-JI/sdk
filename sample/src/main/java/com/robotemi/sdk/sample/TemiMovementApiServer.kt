package com.robotemi.sdk.sample

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.robotemi.sdk.Robot
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

/**
 * REST API Server for remote control of Temi robot movement.
 * Provides three endpoints:
 * 1. POST /api/turn - Turn the robot by specified degrees
 * 2. POST /api/tilt - Tilt the robot head to specified angle
 * 3. POST /api/skidJoy - Control robot movement with linear/angular velocity
 */
class TemiMovementApiServer(port: Int = 7755) : NanoHTTPD(port) {

    companion object {
        private const val TAG = "TemiMovementAPI"
        private const val DEFAULT_SPEED = 1.0f
        private const val DEFAULT_SKID_DURATION_MS = 500L
    }

    private val robot: Robot by lazy { Robot.getInstance() }
    private val gson = Gson()

    /**
     * Data classes for API request/response
     */
    data class TurnRequest(
        val degrees: Float,
        val speed: Float = DEFAULT_SPEED
    )

    data class TiltRequest(
        val angle: Float,
        val speed: Float = DEFAULT_SPEED
    )

    data class SkidJoyRequest(
        val speedX: Float,
        val speedY: Float,
        val durationMs: Long = DEFAULT_SKID_DURATION_MS,
        val smart: Boolean = true
    )

    data class ApiResponse(
        val success: Boolean,
        val message: String,
        val data: Any? = null
    )

    data class ErrorResponse(
        val success: Boolean = false,
        val error: String,
        val details: String? = null
    )

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        Log.d(TAG, "Received request: $method $uri")

        return try {
            when {
                uri == "/api/turn" && method == Method.POST -> handleTurnRequest(session)
                uri == "/api/tilt" && method == Method.POST -> handleTiltRequest(session)
                uri == "/api/skidJoy" && method == Method.POST -> handleSkidJoyRequest(session)
                uri == "/api/status" && method == Method.GET -> handleStatusRequest()
                uri == "/" && method == Method.GET -> handleRootRequest()
                else -> createErrorResponse(404, "Endpoint not found", "Available endpoints: POST /api/turn, POST /api/tilt, POST /api/skidJoy, GET /api/status")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling request", e)
            createErrorResponse(500, "Internal server error", e.message)
        }
    }

    /**
     * Handle robot turn request
     * Expected JSON: {"degrees": 90.0, "speed": 1.0}
     */
    private fun handleTurnRequest(session: IHTTPSession): Response {
        return try {
            val requestBody = getRequestBody(session)
            val request = gson.fromJson(requestBody, TurnRequest::class.java)
            
            // Validate input
            if (request.degrees < -360 || request.degrees > 360) {
                return createErrorResponse(400, "Invalid degrees", "Degrees must be between -360 and 360")
            }
            if (request.speed <= 0 || request.speed > 10) {
                return createErrorResponse(400, "Invalid speed", "Speed must be between 0.1 and 10.0")
            }

            // Execute turn command
            robot.turnBy(request.degrees.toInt(), request.speed)
            
            val response = ApiResponse(
                success = true,
                message = "Turn command executed successfully",
                data = mapOf(
                    "degrees" to request.degrees,
                    "speed" to request.speed
                )
            )
            
            Log.d(TAG, "Turn executed: ${request.degrees}° at speed ${request.speed}")
            createSuccessResponse(response)
            
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "Invalid JSON format", "Expected: {\"degrees\": 90.0, \"speed\": 1.0}")
        }
    }

    /**
     * Handle robot tilt request
     * Expected JSON: {"angle": 23.0, "speed": 1.0}
     */
    private fun handleTiltRequest(session: IHTTPSession): Response {
        return try {
            val requestBody = getRequestBody(session)
            val request = gson.fromJson(requestBody, TiltRequest::class.java)
            
            // Validate input
            if (request.angle < -25 || request.angle > 55) {
                return createErrorResponse(400, "Invalid angle", "Tilt angle must be between -25 and 55 degrees")
            }
            if (request.speed <= 0 || request.speed > 10) {
                return createErrorResponse(400, "Invalid speed", "Speed must be between 0.1 and 10.0")
            }

            // Execute tilt command
            robot.tiltAngle(request.angle.toInt(), request.speed)
            
            val response = ApiResponse(
                success = true,
                message = "Tilt command executed successfully",
                data = mapOf(
                    "angle" to request.angle,
                    "speed" to request.speed
                )
            )
            
            Log.d(TAG, "Tilt executed: ${request.angle}° at speed ${request.speed}")
            createSuccessResponse(response)
            
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "Invalid JSON format", "Expected: {\"angle\": 23.0, \"speed\": 1.0}")
        }
    }

    /**
     * Handle robot skidJoy request
     * Expected JSON: {"speedX": 0.5, "speedY": 0.0, "durationMs": 500, "smart": true}
     */
    private fun handleSkidJoyRequest(session: IHTTPSession): Response {
        return try {
            val requestBody = getRequestBody(session)
            val request = gson.fromJson(requestBody, SkidJoyRequest::class.java)
            
            // Validate input
            if (request.speedX < -1.0 || request.speedX > 1.0) {
                return createErrorResponse(400, "Invalid speedX", "speedX must be between -1.0 and 1.0")
            }
            if (request.speedY < -1.0 || request.speedY > 1.0) {
                return createErrorResponse(400, "Invalid speedY", "speedY must be between -1.0 and 1.0")
            }
            if (request.durationMs <= 0 || request.durationMs > 10000) {
                return createErrorResponse(400, "Invalid duration", "Duration must be between 1 and 10000 milliseconds")
            }

            // Execute skidJoy command in a separate thread to avoid blocking
            Thread {
                val startTime = System.currentTimeMillis()
                val endTime = startTime + request.durationMs
                
                Log.d(TAG, "Starting skidJoy: speedX=${request.speedX}, speedY=${request.speedY}, duration=${request.durationMs}ms, smart=${request.smart}")
                
                while (System.currentTimeMillis() < endTime) {
                    robot.skidJoy(request.speedX, request.speedY, request.smart)
                    Thread.sleep(50) // Small delay to prevent overwhelming the robot
                }
                
                Log.d(TAG, "SkidJoy completed")
            }.start()
            
            val response = ApiResponse(
                success = true,
                message = "SkidJoy command started successfully",
                data = mapOf(
                    "speedX" to request.speedX,
                    "speedY" to request.speedY,
                    "durationMs" to request.durationMs,
                    "smart" to request.smart
                )
            )
            
            createSuccessResponse(response)
            
        } catch (e: JsonSyntaxException) {
            createErrorResponse(400, "Invalid JSON format", "Expected: {\"speedX\": 0.5, \"speedY\": 0.0, \"durationMs\": 500, \"smart\": true}")
        }
    }

    /**
     * Handle status request - returns basic robot information
     */
    private fun handleStatusRequest(): Response {
        return try {
            val position = robot.getPosition()
            val batteryData = robot.batteryData
            
            val statusData = mapOf(
                "position" to mapOf(
                    "x" to position.x,
                    "y" to position.y,
                    "yaw" to position.yaw,
                    "tiltAngle" to position.tiltAngle
                ),
                "battery" to mapOf(
                    "level" to (batteryData?.level ?: -1),
                    "isCharging" to (batteryData?.isCharging ?: false)
                ),
                "serverInfo" to mapOf(
                    "version" to "1.0.0",
                    "endpoints" to listOf("/api/turn", "/api/tilt", "/api/skidJoy", "/api/status")
                )
            )
            
            val response = ApiResponse(
                success = true,
                message = "Robot status retrieved successfully",
                data = statusData
            )
            
            createSuccessResponse(response)
            
        } catch (e: Exception) {
            createErrorResponse(500, "Failed to get robot status", e.message)
        }
    }

    /**
     * Handle root request - returns API documentation
     */
    private fun handleRootRequest(): Response {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Temi Movement API</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .endpoint { background: #f5f5f5; padding: 20px; margin: 10px 0; border-radius: 5px; }
                    .method { color: #2196F3; font-weight: bold; }
                    .path { color: #4CAF50; font-weight: bold; }
                    pre { background: #333; color: #fff; padding: 10px; border-radius: 3px; overflow-x: auto; }
                </style>
            </head>
            <body>
                <h1>Temi Movement API</h1>
                <p>REST API for controlling Temi robot movement remotely.</p>
                
                <div class="endpoint">
                    <h3><span class="method">POST</span> <span class="path">/api/turn</span></h3>
                    <p>Turn the robot by specified degrees</p>
                    <pre>{
  "degrees": 90.0,    // Degrees to turn (-360 to 360)
  "speed": 1.0        // Speed (0.1 to 10.0, optional, default: 1.0)
}</pre>
                </div>
                
                <div class="endpoint">
                    <h3><span class="method">POST</span> <span class="path">/api/tilt</span></h3>
                    <p>Tilt the robot head to specified angle</p>
                    <pre>{
  "angle": 23.0,      // Tilt angle (-25 to 55 degrees)
  "speed": 1.0        // Speed (0.1 to 10.0, optional, default: 1.0)
}</pre>
                </div>
                
                <div class="endpoint">
                    <h3><span class="method">POST</span> <span class="path">/api/skidJoy</span></h3>
                    <p>Control robot movement with linear/angular velocity</p>
                    <pre>{
  "speedX": 0.5,      // Linear velocity (-1.0 to 1.0)
  "speedY": 0.0,      // Angular velocity (-1.0 to 1.0)
  "durationMs": 500,  // Duration in milliseconds (1 to 10000, optional, default: 500)
  "smart": true       // Use smart movement (optional, default: true)
}</pre>
                </div>
                
                <div class="endpoint">
                    <h3><span class="method">GET</span> <span class="path">/api/status</span></h3>
                    <p>Get current robot status (position, battery, etc.)</p>
                </div>
                
                <h3>Example Usage</h3>
                <pre>curl -X POST http://ROBOT_IP:7755/api/turn \
  -H "Content-Type: application/json" \
  -d '{"degrees": 90, "speed": 1.0}'</pre>
            </body>
            </html>
        """.trimIndent()
        
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }

    /**
     * Extract request body from HTTP session
     */
    private fun getRequestBody(session: IHTTPSession): String {
        val map = HashMap<String, String>()
        session.parseBody(map)
        return map["postData"] ?: "{}"
    }

    /**
     * Create success response
     */
    private fun createSuccessResponse(data: Any): Response {
        val json = gson.toJson(data)
        return newFixedLengthResponse(Response.Status.OK, "application/json", json)
    }

    /**
     * Create error response
     */
    private fun createErrorResponse(statusCode: Int, error: String, details: String? = null): Response {
        val status = when (statusCode) {
            400 -> Response.Status.BAD_REQUEST
            404 -> Response.Status.NOT_FOUND
            500 -> Response.Status.INTERNAL_ERROR
            else -> Response.Status.BAD_REQUEST
        }
        
        val errorResponse = ErrorResponse(error = error, details = details)
        val json = gson.toJson(errorResponse)
        return newFixedLengthResponse(status, "application/json", json)
    }

    /**
     * Start the server
     */
    fun startServer(): Boolean {
        return try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            Log.i(TAG, "Temi Movement API Server started on port ${listeningPort}")
            Log.i(TAG, "API Documentation available at: http://localhost:${listeningPort}/")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start server", e)
            false
        }
    }

    /**
     * Stop the server
     */
    fun stopServer() {
        stop()
        Log.i(TAG, "Temi Movement API Server stopped")
    }
}
