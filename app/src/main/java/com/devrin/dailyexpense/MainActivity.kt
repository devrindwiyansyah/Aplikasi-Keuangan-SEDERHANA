package com.devrin.dailyexpense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devrin.dailyexpense.ui.screen.AddTransactionScreen
import com.devrin.dailyexpense.ui.screen.MainScreen
import com.devrin.dailyexpense.ui.theme.DailyExpenseTheme
import com.devrin.dailyexpense.viewmodel.TransactionViewModel
import com.devrin.dailyexpense.viewmodel.TransactionViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val application = application as DailyExpenseApplication
        val viewModelFactory = TransactionViewModelFactory(application.repository)

        setContent {
            DailyExpenseTheme {
                val navController = rememberNavController()
                val transactionViewModel: TransactionViewModel =
                    viewModel(factory = viewModelFactory)

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            viewModel = transactionViewModel,
                            onNavigateToAdd = {
                                navController.navigate("add_transaction")
                            }
                        )
                    }
                    composable("add_transaction") {
                        AddTransactionScreen(
                            viewModel = transactionViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
