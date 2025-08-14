# Temi Movement API Test Examples

This document contains example API calls for testing the Temi Movement REST API.

## Prerequisites

1. Ensure the Temi robot sample app is running
2. Start the API server using the "Start API Server" button in the app
3. Replace `ROBOT_IP` with the actual IP address of your Temi robot
4. The server runs on port 7755 by default

## API Endpoints

### 1. Get API Documentation
```bash
# Open in browser or use curl
curl http://ROBOT_IP:7755/
```

### 2. Get Robot Status
```bash
curl -X GET http://ROBOT_IP:7755/api/status
```

### 3. Turn Robot
```bash
# Turn right 90 degrees at normal speed
curl -X POST http://ROBOT_IP:7755/api/turn \
  -H "Content-Type: application/json" \
  -d '{"degrees": 90, "speed": 1.0}'

# Turn left 45 degrees at slow speed
curl -X POST http://ROBOT_IP:7755/api/turn \
  -H "Content-Type: application/json" \
  -d '{"degrees": -45, "speed": 0.5}'

# Quick 180 degree turn
curl -X POST http://ROBOT_IP:7755/api/turn \
  -H "Content-Type: application/json" \
  -d '{"degrees": 180, "speed": 2.0}'
```

### 4. Tilt Robot Head
```bash
# Tilt head up to 30 degrees
curl -X POST http://ROBOT_IP:7755/api/tilt \
  -H "Content-Type: application/json" \
  -d '{"angle": 30, "speed": 1.0}'

# Tilt head down to -10 degrees
curl -X POST http://ROBOT_IP:7755/api/tilt \
  -H "Content-Type: application/json" \
  -d '{"angle": -10, "speed": 1.5}'

# Return to neutral position
curl -X POST http://ROBOT_IP:7755/api/tilt \
  -H "Content-Type: application/json" \
  -d '{"angle": 0, "speed": 1.0}'
```

### 5. Control Robot Movement (SkidJoy)
```bash
# Move forward for 1 second
curl -X POST http://ROBOT_IP:7755/api/skidJoy \
  -H "Content-Type: application/json" \
  -d '{"speedX": 0.5, "speedY": 0.0, "durationMs": 1000, "smart": true}'

# Move backward for 500ms
curl -X POST http://ROBOT_IP:7755/api/skidJoy \
  -H "Content-Type: application/json" \
  -d '{"speedX": -0.3, "speedY": 0.0, "durationMs": 500, "smart": true}'

# Rotate left while moving forward
curl -X POST http://ROBOT_IP:7755/api/skidJoy \
  -H "Content-Type: application/json" \
  -d '{"speedX": 0.4, "speedY": 0.5, "durationMs": 800, "smart": true}'

# Pure rotation (no forward movement)
curl -X POST http://ROBOT_IP:7755/api/skidJoy \
  -H "Content-Type: application/json" \
  -d '{"speedX": 0.0, "speedY": 0.8, "durationMs": 600, "smart": true}'
```

## Parameter Ranges

### Turn API
- `degrees`: -360 to 360 (negative = left, positive = right)
- `speed`: 0.1 to 10.0 (optional, default: 1.0)

### Tilt API
- `angle`: -25 to 55 degrees (negative = down, positive = up)
- `speed`: 0.1 to 10.0 (optional, default: 1.0)

### SkidJoy API
- `speedX`: -1.0 to 1.0 (negative = backward, positive = forward)
- `speedY`: -1.0 to 1.0 (negative = rotate right, positive = rotate left)
- `durationMs`: 1 to 10000 milliseconds (optional, default: 500)
- `smart`: true/false (optional, default: true) - enables smart obstacle avoidance

## Response Format

### Success Response
```json
{
  "success": true,
  "message": "Command executed successfully",
  "data": {
    "degrees": 90,
    "speed": 1.0
  }
}
```

### Error Response
```json
{
  "success": false,
  "error": "Invalid degrees",
  "details": "Degrees must be between -360 and 360"
}
```

## Testing with Python

```python
import requests
import json

# Robot IP address
ROBOT_IP = "192.168.1.100"  # Replace with actual IP
BASE_URL = f"http://{ROBOT_IP}:7755"

def test_turn(degrees, speed=1.0):
    response = requests.post(f"{BASE_URL}/api/turn", 
                           json={"degrees": degrees, "speed": speed})
    print(f"Turn {degrees}°: {response.json()}")

def test_tilt(angle, speed=1.0):
    response = requests.post(f"{BASE_URL}/api/tilt", 
                           json={"angle": angle, "speed": speed})
    print(f"Tilt {angle}°: {response.json()}")

def test_movement(speed_x, speed_y, duration=500):
    response = requests.post(f"{BASE_URL}/api/skidJoy", 
                           json={"speedX": speed_x, "speedY": speed_y, 
                                "durationMs": duration, "smart": True})
    print(f"Move X:{speed_x} Y:{speed_y}: {response.json()}")

def get_status():
    response = requests.get(f"{BASE_URL}/api/status")
    print(f"Status: {response.json()}")

# Test examples
if __name__ == "__main__":
    get_status()
    test_turn(90)
    test_tilt(20)
    test_movement(0.5, 0.0, 1000)
```

## Testing with JavaScript/Node.js

```javascript
const axios = require('axios');

const ROBOT_IP = '192.168.1.100'; // Replace with actual IP
const BASE_URL = `http://${ROBOT_IP}:7755`;

async function testTurn(degrees, speed = 1.0) {
    try {
        const response = await axios.post(`${BASE_URL}/api/turn`, {
            degrees: degrees,
            speed: speed
        });
        console.log(`Turn ${degrees}°:`, response.data);
    } catch (error) {
        console.error('Turn error:', error.response?.data || error.message);
    }
}

async function testTilt(angle, speed = 1.0) {
    try {
        const response = await axios.post(`${BASE_URL}/api/tilt`, {
            angle: angle,
            speed: speed
        });
        console.log(`Tilt ${angle}°:`, response.data);
    } catch (error) {
        console.error('Tilt error:', error.response?.data || error.message);
    }
}

async function testMovement(speedX, speedY, duration = 500) {
    try {
        const response = await axios.post(`${BASE_URL}/api/skidJoy`, {
            speedX: speedX,
            speedY: speedY,
            durationMs: duration,
            smart: true
        });
        console.log(`Move X:${speedX} Y:${speedY}:`, response.data);
    } catch (error) {
        console.error('Movement error:', error.response?.data || error.message);
    }
}

async function getStatus() {
    try {
        const response = await axios.get(`${BASE_URL}/api/status`);
        console.log('Status:', response.data);
    } catch (error) {
        console.error('Status error:', error.response?.data || error.message);
    }
}

// Test examples
async function runTests() {
    await getStatus();
    await testTurn(90);
    await testTilt(20);
    await testMovement(0.5, 0.0, 1000);
}

runTests();
```

## Error Codes

- **400 Bad Request**: Invalid parameters or JSON format
- **404 Not Found**: Endpoint not found
- **500 Internal Server Error**: Server error or robot communication issue

## Notes

1. The API server runs asynchronously and doesn't block the main Temi app
2. Multiple commands can be sent, but be careful not to overwhelm the robot
3. The `smart` parameter in skidJoy enables obstacle avoidance
4. All speeds and angles are validated before execution
5. The server includes CORS headers for web-based clients
