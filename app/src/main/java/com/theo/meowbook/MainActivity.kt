package com.theo.meowbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.theo.meowbook.ui.details.CatDetailsDestination
import com.theo.meowbook.ui.details.CatDetailsScreen
import com.theo.meowbook.ui.listing.CatListDestination
import com.theo.meowbook.ui.listing.CatListScreen
import com.theo.meowbook.ui.theme.MeowBookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeowBookTheme {
                val navController = rememberNavController()

                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = CatListDestination,
                ) {
                    composable<CatListDestination> {
                        CatListScreen(
                            onCatClick = { catId ->
                                navController.navigate(CatDetailsDestination(id = catId))
                            }
                        )
                    }

                    composable<CatDetailsDestination> {
                        val args = it.toRoute<CatDetailsDestination>()
                        CatDetailsScreen(
                            id = args.id,
                            onBack = { navController.popBackStack(CatListDestination, false) }
                        )
                    }
                }
            }
        }
    }
}
