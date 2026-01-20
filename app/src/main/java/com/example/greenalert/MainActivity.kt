package com.example.greenalert

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.greenalert.ui.navigation.NavGraph
import com.example.greenalert.ui.theme.GreenAlertTheme
import com.example.greenalert.util.MapShareParser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Parse shared content from Maps
        var sharedAddress: String? = null
        var sharedLat: Double? = null
        var sharedLng: Double? = null
        
        intent?.let { handleIntent(it) }?.let { parsed ->
            when (parsed) {
                is MapShareParser.ParsedLocation.Address -> {
                    sharedAddress = parsed.address
                }
                is MapShareParser.ParsedLocation.Coordinates -> {
                    sharedLat = parsed.latitude
                    sharedLng = parsed.longitude
                }
            }
        }
        
        val finalAddress = sharedAddress
        val finalLat = sharedLat
        val finalLng = sharedLng
        
        setContent {
            GreenAlertTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavGraph(
                        navController = navController,
                        sharedAddress = finalAddress,
                        sharedLat = finalLat,
                        sharedLng = finalLng
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle new intents when app is already running
        // You could emit an event to a ViewModel here for more complex handling
    }
    
    private fun handleIntent(intent: Intent): MapShareParser.ParsedLocation? {
        return MapShareParser.parseSharedContent(intent)
    }
}