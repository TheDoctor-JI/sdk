# Talking Face Animation API Testing

This document shows how to test the new talking face animation endpoint.

## New Endpoint

### Toggle Speaking State
```bash
# Start talking animation (video loops)
curl -X POST http://ROBOT_IP:7755/api/toggle_speaking_state \
  -H "Content-Type: application/json" \
  -d '{"is_speaking": true}'

# Stop talking animation (show first frame)
curl -X POST http://ROBOT_IP:7755/api/toggle_speaking_state \
  -H "Content-Type: application/json" \
  -d '{"is_speaking": false}'
```

## Testing Sequence

1. **Start the API Server**
   - Open the Temi sample app
   - Go to Settings & Status tab
   - Click "Start API Server" 
   - The app will switch to a black screen with the talking face video

2. **Test the Animation**
   ```bash
   # Make the face start talking
   curl -X POST http://ROBOT_IP:7755/api/toggle_speaking_state \
     -H "Content-Type: application/json" \
     -d '{"is_speaking": true}'
   
   # Wait a few seconds to see the looping animation
   
   # Stop the talking
   curl -X POST http://ROBOT_IP:7755/api/toggle_speaking_state \
     -H "Content-Type: application/json" \
     -d '{"is_speaking": false}'
   ```

3. **Integrate with Movement**
   ```bash
   # Start talking while turning
   curl -X POST http://ROBOT_IP:7755/api/toggle_speaking_state \
     -H "Content-Type: application/json" \
     -d '{"is_speaking": true}'
   
   curl -X POST http://ROBOT_IP:7755/api/turn \
     -H "Content-Type: application/json" \
     -d '{"degrees": 45, "speed": 1.0}'
   
   # Stop talking after movement
   curl -X POST http://ROBOT_IP:7755/api/toggle_speaking_state \
     -H "Content-Type: application/json" \
     -d '{"is_speaking": false}'
   ```

## Python Example

```python
import requests
import time

ROBOT_IP = "192.168.1.100"  # Replace with actual IP
BASE_URL = f"http://{ROBOT_IP}:7755"

def set_speaking(is_speaking):
    response = requests.post(f"{BASE_URL}/api/toggle_speaking_state", 
                           json={"is_speaking": is_speaking})
    print(f"Speaking {is_speaking}: {response.json()}")

def turn_robot(degrees):
    response = requests.post(f"{BASE_URL}/api/turn", 
                           json={"degrees": degrees, "speed": 1.0})
    print(f"Turn {degrees}°: {response.json()}")

# Example dialogue sequence
def simulate_dialogue():
    print("Starting dialogue simulation...")
    
    # Robot starts speaking
    set_speaking(True)
    time.sleep(3)  # Simulate 3 seconds of speech
    
    # Robot turns while talking
    turn_robot(90)
    time.sleep(2)
    
    # Robot stops speaking
    set_speaking(False)
    time.sleep(1)
    
    # Robot speaks again
    set_speaking(True)
    time.sleep(2)
    set_speaking(False)
    
    print("Dialogue simulation complete!")

if __name__ == "__main__":
    simulate_dialogue()
```

## JavaScript Example

```javascript
const axios = require('axios');

const ROBOT_IP = '192.168.1.100'; // Replace with actual IP
const BASE_URL = `http://${ROBOT_IP}:7755`;

async function setSpeaking(isSpeaking) {
    try {
        const response = await axios.post(`${BASE_URL}/api/toggle_speaking_state`, {
            is_speaking: isSpeaking
        });
        console.log(`Speaking ${isSpeaking}:`, response.data);
    } catch (error) {
        console.error('Speaking error:', error.response?.data || error.message);
    }
}

async function turnRobot(degrees) {
    try {
        const response = await axios.post(`${BASE_URL}/api/turn`, {
            degrees: degrees,
            speed: 1.0
        });
        console.log(`Turn ${degrees}°:`, response.data);
    } catch (error) {
        console.error('Turn error:', error.response?.data || error.message);
    }
}

async function simulateDialogue() {
    console.log('Starting dialogue simulation...');
    
    // Robot starts speaking
    await setSpeaking(true);
    await new Promise(resolve => setTimeout(resolve, 3000)); // 3 seconds
    
    // Robot turns while talking
    await turnRobot(90);
    await new Promise(resolve => setTimeout(resolve, 2000)); // 2 seconds
    
    // Robot stops speaking
    await setSpeaking(false);
    await new Promise(resolve => setTimeout(resolve, 1000)); // 1 second
    
    // Robot speaks again
    await setSpeaking(true);
    await new Promise(resolve => setTimeout(resolve, 2000)); // 2 seconds
    await setSpeaking(false);
    
    console.log('Dialogue simulation complete!');
}

simulateDialogue();
```

## Response Format

### Success Response
```json
{
  "success": true,
  "message": "Speaking state updated successfully",
  "data": {
    "is_speaking": true
  }
}
```

### Error Response
```json
{
  "success": false,
  "error": "Invalid JSON format",
  "details": "Expected: {\"is_speaking\": true}"
}
```

## Features

- **MP4 Video Support**: Uses Android's native VideoView for smooth playback
- **Automatic Looping**: When speaking=true, video loops continuously
- **First Frame Display**: When speaking=false, shows first frame only
- **Lifecycle Management**: Properly handles activity pause/resume/destroy
- **Real-time Control**: Immediate response to API calls
- **Debug Information**: Shows current animation state on screen

## Notes

- The video file `talking_face_ref.mp4` should be placed in `res/raw/` folder
- The API server runs on port 7755
- The animation state persists across API calls until changed
- Video automatically pauses when the activity goes to background
- All video operations are performed on the UI thread for smooth animation
