# Temi Robot REST API Implementation

This implementation adds REST API endpoints to the Temi robot sample app for remote movement control. The API allows external systems to control the robot's movement through HTTP requests.

## Files Added/Modified

### New Files
1. **`TemiMovementApiServer.kt`** - Main REST API server implementation
2. **`API_USAGE_EXAMPLES.md`** - Complete usage documentation and examples

### Modified Files
1. **`MainActivity.kt`** - Added API server integration and controls
2. **`group_settings_and_status.xml`** - Added server control buttons
3. **`build.gradle`** - Added NanoHTTPD dependency
4. **`AndroidManifest.xml`** - Added INTERNET permission

## Architecture

### Clean Separation of Concerns
- **`TemiMovementApiServer`**: Standalone HTTP server class, completely separate from UI
- **`MainActivity`**: Only handles server lifecycle (start/stop), no API logic
- **REST endpoints**: Clean JSON API with proper error handling and validation

### HTTP Server
- Uses **NanoHTTPD** - lightweight, reliable HTTP server for Android
- Runs on port **7755** by default
- Asynchronous operation - doesn't block the main app

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | API documentation (HTML) |
| `GET` | `/api/status` | Robot status (position, battery, etc.) |
| `POST` | `/api/turn` | Turn robot by degrees |
| `POST` | `/api/tilt` | Tilt robot head to angle |
| `POST` | `/api/skidJoy` | Control robot movement |

## Request/Response Format

### Turn Robot
```json
POST /api/turn
{
    "degrees": 90.0,     // -360 to 360 degrees
    "speed": 1.0         // 0.1 to 10.0 (optional)
}
```

### Tilt Robot Head
```json
POST /api/tilt
{
    "angle": 23.0,       // -25 to 55 degrees
    "speed": 1.0         // 0.1 to 10.0 (optional)
}
```

### Control Movement
```json
POST /api/skidJoy
{
    "speedX": 0.5,       // -1.0 to 1.0 (linear velocity)
    "speedY": 0.0,       // -1.0 to 1.0 (angular velocity)
    "durationMs": 500,   // 1 to 10000 ms (optional)
    "smart": true        // Enable obstacle avoidance (optional)
}
```

## Key Features

### 1. Input Validation
- All parameters are validated before execution
- Clear error messages for invalid inputs
- Prevents dangerous commands (excessive speeds, angles)

### 2. Error Handling
- Comprehensive error responses with details
- Proper HTTP status codes (400, 404, 500)
- Graceful handling of robot communication failures

### 3. Safety Features
- Speed and angle limits to prevent robot damage
- Smart movement option for obstacle avoidance
- Duration limits for skidJoy commands

### 4. Documentation
- Built-in HTML documentation at root endpoint
- Complete example usage in multiple languages
- Clear parameter descriptions and ranges

### 5. Status Monitoring
- Real-time position and battery information
- Server status display in the main app
- Comprehensive logging for debugging

## Usage in Your Dialogue System

### Python Example
```python
import requests

def move_robot(action, **params):
    response = requests.post(f"http://{ROBOT_IP}:7755/api/{action}", 
                           json=params)
    return response.json()

# Examples
move_robot("turn", degrees=90, speed=1.0)
move_robot("tilt", angle=20, speed=0.5)
move_robot("skidJoy", speedX=0.5, speedY=0.0, durationMs=1000)
```

### JavaScript Example
```javascript
async function moveRobot(action, params) {
    const response = await fetch(`http://${ROBOT_IP}:7755/api/${action}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(params)
    });
    return response.json();
}

// Examples
await moveRobot('turn', {degrees: -45, speed: 1.5});
await moveRobot('tilt', {angle: 30});
await moveRobot('skidJoy', {speedX: 0.3, speedY: 0.2, durationMs: 800});
```

## Integration Steps

1. **Build the app** with the new dependencies
2. **Install and run** the sample app on Temi
3. **Start the API server** using the button in Settings & Status
4. **Find the robot's IP address** (shown in network settings)
5. **Test the API** using the provided examples

## Server Controls

The main app now includes:
- **Start API Server** button - Launches the HTTP server
- **Stop API Server** button - Stops the HTTP server
- **Status indicator** - Shows current server state
- **Automatic cleanup** - Server stops when app closes

## Development Benefits

### For Your Dialogue System
- **Simple HTTP interface** - No Android development required
- **Language agnostic** - Use any programming language
- **Real-time control** - Immediate robot response
- **Stateless operation** - No connection management needed

### For Debugging
- **Browser-accessible documentation** - Visit `http://ROBOT_IP:7755`
- **Comprehensive logging** - All requests logged in the app
- **Status endpoint** - Monitor robot state remotely

### For Production
- **Error resilience** - Robust error handling and recovery
- **Clean architecture** - Easy to maintain and extend
- **Performance optimized** - Minimal impact on robot resources

## Future Extensions

The current implementation can be easily extended with additional endpoints:
- **Navigation control** (go to location, follow mode)
- **Speech control** (text-to-speech commands)
- **Sensor data** (camera feed, detection events)
- **System control** (volume, settings)

The modular design makes it simple to add new functionality without affecting existing features.

## Security Considerations

- **Local network only** - Server binds to all interfaces but should be firewalled
- **No authentication** - Suitable for trusted local networks
- **Input validation** - Prevents malformed requests from causing issues

For production deployment, consider adding:
- API key authentication
- HTTPS support
- Rate limiting
- Request logging/audit trail
