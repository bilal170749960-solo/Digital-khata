package com.example.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.UserRole
import com.example.ui.customer.CustomerSettingsScreen
import com.example.ui.shopkeeper.ShopkeeperSettingsScreen
import com.example.ui.viewmodel.KhataViewModel

/**
 * Common entry point for Settings.
 * Automatically displays the dedicated ShopkeeperSettingsScreen or CustomerSettingsScreen
 * based on the authenticated user's role.
 */
@Composable
fun SettingsScreen(
    viewModel: KhataViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser?.role == UserRole.SHOPKEEPER) {
        ShopkeeperSettingsScreen(
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        CustomerSettingsScreen(
            viewModel = viewModel,
            modifier = modifier
        )
    }
}
