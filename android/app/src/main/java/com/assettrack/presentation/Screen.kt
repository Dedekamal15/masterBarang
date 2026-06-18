package com.assettrack.presentation

sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object Registration : Screen("registration")
    object Transaction  : Screen("transaction")
    object History      : Screen("history")
}
