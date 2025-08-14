package com.robotemi.sdk.sample

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.robotemi.sdk.sample.databinding.ActivityApiServerBinding

/**
 * Activity that displays when the API server is running.
 * Shows a talking face video that can be controlled via the API.
 */
class ApiServerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ApiServerActivity"
    }

    private lateinit var binding: ActivityApiServerBinding
    private var apiServer: TemiMovementApiServer? = null
    private lateinit var talkingFaceVideoView: VideoView
    private var isSpeaking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApiServerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Hide system UI for fullscreen experience
        hideSystemUI()
        
        // Initialize the talking face video
        setupTalkingFaceVideo()
        
        // Get the API server instance from MainActivity
        MainActivity.tempApiServerInstance?.let { server ->
            setApiServer(server)
        }
        
        // Setup back button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Setup stop server button
        binding.btnStopServer.setOnClickListener {
            stopApiServer()
            finish()
        }
        
        Log.d(TAG, "ApiServerActivity created")
    }

    private fun hideSystemUI() {
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }

    private fun setupTalkingFaceVideo() {
        talkingFaceVideoView = binding.vvTalkingFace
        
        try {
            // Load the MP4 video from raw resources
            val uri = Uri.parse("android.resource://$packageName/${R.raw.talking_face_ref}")
            talkingFaceVideoView.setVideoURI(uri)
            
            // Set up video completion listener to loop when speaking
            talkingFaceVideoView.setOnCompletionListener { mediaPlayer ->
                if (isSpeaking) {
                    // Loop the video if we're in speaking mode
                    talkingFaceVideoView.start()
                    Log.d(TAG, "Video looped")
                }
            }
            
            // Set up prepared listener to show first frame
            talkingFaceVideoView.setOnPreparedListener { mediaPlayer ->
                // Pause immediately to show first frame (not speaking state)
                mediaPlayer.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                talkingFaceVideoView.pause()
                Log.d(TAG, "Video prepared and paused at first frame")
            }
            
            // Set initial state (not speaking - first frame)
            setSpeakingState(false)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up talking face video", e)
        }
    }

    /**
     * Control the speaking animation state
     */
    fun setSpeakingState(isSpeaking: Boolean) {
        this.isSpeaking = isSpeaking
        
        runOnUiThread {
            try {
                if (isSpeaking) {
                    // Start video playback (will loop due to completion listener)
                    if (!talkingFaceVideoView.isPlaying) {
                        talkingFaceVideoView.start()
                    }
                    binding.tvDebugInfo.text = "Animation: Speaking"
                    Log.d(TAG, "Started talking video")
                } else {
                    // Pause video and seek to beginning (first frame)
                    talkingFaceVideoView.pause()
                    talkingFaceVideoView.seekTo(0)
                    binding.tvDebugInfo.text = "Animation: Stopped"
                    Log.d(TAG, "Stopped talking video")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error controlling video", e)
            }
        }
    }

    /**
     * Set the API server instance so we can control it
     */
    fun setApiServer(server: TemiMovementApiServer) {
        this.apiServer = server
        // Register this activity with the server so it can control the animation
        server.setAnimationController(this)
    }

    private fun stopApiServer() {
        apiServer?.stopServer()
    }

    override fun onDestroy() {
        // Stop video when activity is destroyed
        if (::talkingFaceVideoView.isInitialized) {
            talkingFaceVideoView.stopPlayback()
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        // Pause video when activity goes to background
        if (::talkingFaceVideoView.isInitialized && talkingFaceVideoView.isPlaying) {
            talkingFaceVideoView.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume video if we were in speaking state
        if (isSpeaking && ::talkingFaceVideoView.isInitialized) {
            talkingFaceVideoView.start()
        }
    }
}
