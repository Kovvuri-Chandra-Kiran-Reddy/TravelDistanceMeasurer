package com.example.traveldistancemeasurer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.traveldistancemeasurer.ui.navigation.NavGraph
import com.example.traveldistancemeasurer.ui.theme.TravelDistanceMeasurerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Travel Distance Measurer app
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelDistanceMeasurerTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
