package com.desimeals.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.desimeals.app.di.AppContainer
import com.desimeals.app.ui.screens.AddTransactionScreen
import com.desimeals.app.ui.screens.CustomerDetailScreen
import com.desimeals.app.ui.screens.CustomersScreen
import com.desimeals.app.ui.screens.DashboardScreen
import com.desimeals.app.ui.screens.LedgerScreen
import com.desimeals.app.ui.screens.MealsScreen
import com.desimeals.app.ui.viewmodel.CustomerDetailViewModel
import com.desimeals.app.ui.viewmodel.DesiViewModelFactory

object Routes {
    const val DASHBOARD = "dashboard"
    const val CUSTOMERS = "customers"
    const val CUSTOMER_DETAIL = "customer/{customerId}"
    const val LEDGER = "ledger"
    const val MEALS = "meals"
    const val ADD_TRANSACTION = "add_transaction?customerId={customerId}"

    fun customerDetail(id: Long) = "customer/$id"
    fun addTransaction(customerId: Long? = null) =
        if (customerId == null) "add_transaction?customerId=-1" else "add_transaction?customerId=$customerId"
}

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

val bottomItems = listOf(
    BottomItem(Routes.DASHBOARD, "Home", Icons.Filled.Dashboard),
    BottomItem(Routes.CUSTOMERS, "Members", Icons.Filled.People),
    BottomItem(Routes.MEALS, "Meals", Icons.Filled.Restaurant),
    BottomItem(Routes.LEDGER, "Ledger", Icons.Filled.ReceiptLong)
)

@Composable
fun DesiMealsNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val factory = DesiViewModelFactory(container)
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    factory = factory,
                    onNavigateToCustomers = { navController.navigate(Routes.CUSTOMERS) },
                    onNavigateToLedger = { navController.navigate(Routes.LEDGER) },
                    onNavigateToMeals = { navController.navigate(Routes.MEALS) },
                    onCustomerClick = { navController.navigate(Routes.customerDetail(it)) }
                )
            }
            composable(Routes.CUSTOMERS) {
                CustomersScreen(
                    factory = factory,
                    onCustomerClick = { navController.navigate(Routes.customerDetail(it)) }
                )
            }
            composable(
                Routes.CUSTOMER_DETAIL,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("customerId") ?: return@composable
                val detailFactory = factory.customerDetail(id)
                val vm: CustomerDetailViewModel = viewModel(factory = detailFactory)
                CustomerDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onAddTransaction = { navController.navigate(Routes.addTransaction(id)) }
                )
            }
            composable(Routes.MEALS) {
                MealsScreen(factory = factory)
            }
            composable(Routes.LEDGER) {
                LedgerScreen(
                    factory = factory,
                    onAdd = { navController.navigate(Routes.addTransaction()) }
                )
            }
            composable(
                Routes.ADD_TRANSACTION,
                arguments = listOf(
                    navArgument("customerId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                val cid = entry.arguments?.getLong("customerId")?.takeIf { it != -1L }
                AddTransactionScreen(
                    factory = factory,
                    container = container,
                    preselectedCustomerId = cid,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}
