package com.desimeals.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.desimeals.app.ui.screens.*
import com.desimeals.app.ui.viewmodel.AppViewModel

@Composable
fun AppNav(vm: AppViewModel) {
    val nav = rememberNavController()
    val tabs = listOf(
        Routes.DASHBOARD to "Home",
        Routes.CUSTOMERS to "Customers",
        Routes.TRANSACTIONS to "Activity",
        Routes.EXPENSES to "Expenses",
        Routes.REPORTS to "Reports"
    )
    var current by remember { mutableStateOf(Routes.SPLASH) }
    nav.addOnDestinationChangedListener { _, d, _ -> current = d.route ?: "" }

    if (current == Routes.SPLASH || current == Routes.ONBOARDING) {
        NavHost(nav, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) { SplashScreen(onDone = { nav.navigate(Routes.DASHBOARD) { popUpTo(0) } }) }
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { (route, label) ->
                    val icon = when (route) {
                        Routes.DASHBOARD -> Icons.Default.Home
                        Routes.CUSTOMERS -> Icons.Default.People
                        Routes.TRANSACTIONS -> Icons.Default.Receipt
                        Routes.EXPENSES -> Icons.Default.ShoppingCart
                        else -> Icons.Default.Assessment
                    }
                    NavigationBarItem(
                        selected = current.startsWith(route),
                        onClick = { nav.navigate(route) { launchSingleTop = true; popUpTo(Routes.DASHBOARD) } },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = Routes.DASHBOARD, modifier = Modifier.padding(pad)) {
            composable(Routes.DASHBOARD) { DashboardScreen(vm, nav) }
            composable(Routes.CUSTOMERS) { CustomerListScreen(vm, nav) }
            composable(Routes.ADD_CUSTOMER) { AddEditCustomerScreen(vm, nav, null) }
            composable(Routes.EDIT_CUSTOMER, listOf(navArgument("id") { type = NavType.LongType })) {
                AddEditCustomerScreen(vm, nav, it.arguments?.getLong("id"))
            }
            composable(Routes.CUSTOMER_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                CustomerDetailScreen(vm, nav, it.arguments?.getLong("id") ?: 0L)
            }
            composable(Routes.LEDGER, listOf(navArgument("id") { type = NavType.LongType })) {
                LedgerScreen(vm, nav, it.arguments?.getLong("id") ?: 0L)
            }
            composable(Routes.ADD_DEBIT, listOf(navArgument("id") { type = NavType.LongType })) {
                AddTxScreen(vm, nav, it.arguments?.getLong("id") ?: 0L, isDebit = true)
            }
            composable(Routes.ADD_CREDIT, listOf(navArgument("id") { type = NavType.LongType })) {
                AddTxScreen(vm, nav, it.arguments?.getLong("id") ?: 0L, isDebit = false)
            }
            composable(Routes.RECEIPT, listOf(navArgument("txId") { type = NavType.LongType })) {
                ReceiptScreen(vm, nav, it.arguments?.getLong("txId") ?: 0L)
            }
            composable(Routes.MEALS) { MealScreen(vm, nav) }
            composable(Routes.INVOICES) { InvoiceListScreen(vm, nav) }
            composable(Routes.INVOICE_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                InvoiceDetailScreen(vm, nav, it.arguments?.getLong("id") ?: 0L)
            }
            composable(Routes.TRANSACTIONS) { TransactionHistoryScreen(vm, nav) }
            composable(Routes.EXPENSES) { ExpenseListScreen(vm, nav) }
            composable(Routes.ADD_EXPENSE) { AddExpenseScreen(vm, nav, null) }
            composable(Routes.EXPENSE_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                ExpenseDetailScreen(vm, nav, it.arguments?.getLong("id") ?: 0L)
            }
            composable(Routes.SUPPLIERS) { SupplierListScreen(vm, nav) }
            composable(Routes.SUPPLIER_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                SupplierDetailScreen(vm, nav, it.arguments?.getLong("id") ?: 0L)
            }
            composable(Routes.CASHBOOK) { CashbookScreen(vm, nav) }
            composable(Routes.ACCOUNTS) { AccountsScreen(vm, nav) }
            composable(Routes.REPORTS) { ReportsScreen(vm, nav) }
            composable(Routes.REPORT_VIEW, listOf(navArgument("kind") { type = NavType.StringType })) {
                ReportViewScreen(vm, nav, it.arguments?.getString("kind") ?: "profit")
            }
            composable(Routes.SEARCH) { SearchScreen(vm, nav) }
            composable(Routes.BACKUP) { BackupScreen(vm, nav) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, nav) }
            composable(Routes.CURRENCY) { CurrencyScreen(vm, nav) }
            composable(Routes.THEME) { ThemeScreen(vm, nav) }
            composable(Routes.ABOUT) { AboutScreen(nav) }
            composable(Routes.PRIVACY) { PrivacyScreen(nav) }
        }
    }
}
