package com.example.ui.shopkeeper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ThemeMode
import com.example.ui.theme.KhataRed
import com.example.ui.viewmodel.KhataViewModel
import com.example.utils.HapticManager
import com.example.utils.NotificationPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopkeeperSettingsScreen(
    viewModel: KhataViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val vibrationEnabled by HapticManager.vibrationEnabled.collectAsState()

    // Dialog States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showShopSettingsDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showReauthDialog by remember { mutableStateOf(false) }
    var showRestoreBackupDialog by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var lastBackupMeta by remember { mutableStateOf(viewModel.getShopkeeperLastBackupMetadata()) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    // Edit Profile Inputs
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editShopName by remember { mutableStateOf("") }

    // Change Password Inputs
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Reauth for Delete
    var reauthPasswordInput by remember { mutableStateOf("") }
    var reauthError by remember { mutableStateOf<String?>(null) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // Notification Preference States
    var notifNewCustomer by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_SHOP_NEW_CUSTOMER))
    }
    var notifNewKhataEntry by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_SHOP_NEW_KHATA_ENTRY))
    }
    var notifPaymentRequest by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_SHOP_PAYMENT_REQUEST))
    }
    var notifPaymentAccepted by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_SHOP_PAYMENT_ACCEPTED))
    }
    var notifPaymentRejected by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_SHOP_PAYMENT_REJECTED))
    }
    var notifPrevBalance by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_SHOP_PREV_BALANCE))
    }
    var notifMonthlyReminder by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_SHOP_MONTHLY_REMINDER))
    }

    Scaffold(
        modifier = modifier.testTag("shopkeeper_settings_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shopkeeper Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. PROFILE SECTION
            item {
                SectionHeader("PROFILE")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.shopName ?: "My Shop",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Shopkeeper: ${currentUser?.name ?: ""}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Email Address",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = currentUser?.email ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Phone Number",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = currentUser?.phone?.ifEmpty { "Not set" } ?: "Not set",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Button(
                            onClick = {
                                editName = currentUser?.name ?: ""
                                editPhone = currentUser?.phone ?: ""
                                editShopName = currentUser?.shopName ?: ""
                                showEditProfileDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("edit_profile_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile & Shop", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 2. SHOP SETTINGS SECTION
            item {
                SectionHeader("SHOP SETTINGS")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SettingRowInfo(
                            icon = Icons.Default.Store,
                            title = "Shop Name",
                            value = currentUser?.shopName ?: "Not set"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        SettingRowInfo(
                            icon = Icons.Default.Phone,
                            title = "Shop WhatsApp / Phone",
                            value = currentUser?.phone?.ifEmpty { "None added" } ?: "None added"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        SettingRowInfo(
                            icon = Icons.Default.Numbers,
                            title = "Khata Numbering",
                            value = "Automatic serial assignment (starts at #101)"
                        )
                    }
                }
            }

            // 3. NOTIFICATIONS SECTION
            item {
                SectionHeader("NOTIFICATION PREFERENCES")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        NotificationToggleRow("New Customer / Activity", "Alert when a new customer is linked or added", notifNewCustomer) {
                            notifNewCustomer = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_SHOP_NEW_CUSTOMER, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("New Khata Entry", "Notify on item purchase records", notifNewKhataEntry) {
                            notifNewKhataEntry = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_SHOP_NEW_KHATA_ENTRY, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Payment Requests", "Notify when a customer submits a payment request", notifPaymentRequest) {
                            notifPaymentRequest = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_SHOP_PAYMENT_REQUEST, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Payment Accepted", "Confirmation alerts on approved payments", notifPaymentAccepted) {
                            notifPaymentAccepted = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_SHOP_PAYMENT_ACCEPTED, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Payment Rejected", "Alerts when payment requests are rejected", notifPaymentRejected) {
                            notifPaymentRejected = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_SHOP_PAYMENT_REJECTED, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Previous Balance Reminder", "Periodic summary of unsettled balances", notifPrevBalance) {
                            notifPrevBalance = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_SHOP_PREV_BALANCE, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Monthly Reminder", "Remind to close monthly bills at end of cycle", notifMonthlyReminder) {
                            notifMonthlyReminder = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_SHOP_MONTHLY_REMINDER, it)
                        }
                    }
                }
            }

            // 4. APPEARANCE SECTION
            item {
                SectionHeader("APPEARANCE")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Theme Preference",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose light, dark, or system default color scheme",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.values().forEach { mode ->
                                val isSelected = themeMode == mode
                                Button(
                                    onClick = { viewModel.setThemeMode(mode) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("shop_theme_button_${mode.name.lowercase()}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            ThemeMode.LIGHT -> Icons.Default.LightMode
                                            ThemeMode.DARK -> Icons.Default.DarkMode
                                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. PREFERENCES (VIBRATION)
            item {
                SectionHeader("PREFERENCES")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Vibration",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Use short haptic feedback when navigating and performing important actions.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { isChecked ->
                                HapticManager.setVibrationEnabled(context, isChecked)
                            },
                            modifier = Modifier.testTag("shop_vibration_switch")
                        )
                    }
                }
            }

            // 6. SECURITY SECTION
            item {
                SectionHeader("SECURITY")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentPasswordInput = ""
                                    newPasswordInput = ""
                                    confirmPasswordInput = ""
                                    passwordError = null
                                    showChangePasswordDialog = true
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Change Password",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Update your account login password",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 7. BACKUP & RESTORE
            item {
                SectionHeader("BACKUP & RESTORE")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("backup_restore_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cloud Backup & Sync",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lastBackupMeta != null)
                                        "Last synced: ${lastBackupMeta!!.dateFormatted} • ${lastBackupMeta!!.customerCount} Customers, ${lastBackupMeta!!.itemCount} Items"
                                    else "No cloud backup created yet.",
                                    fontSize = 12.sp,
                                    color = if (lastBackupMeta != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = if (lastBackupMeta != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (lastBackupMeta != null) "PROTECTED" else "NOT BACKED UP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastBackupMeta != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Backup and Restore Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isBackingUp = true
                                    viewModel.createShopkeeperCloudBackup { res ->
                                        isBackingUp = false
                                        res.onSuccess { summary ->
                                            lastBackupMeta = viewModel.getShopkeeperLastBackupMetadata()
                                            snackbarMessage = summary.message
                                        }.onFailure { err ->
                                            snackbarMessage = err.message ?: "Backup failed"
                                        }
                                    }
                                },
                                enabled = !isBackingUp && !isRestoring,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("create_backup_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isBackingUp) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Backing up...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { showRestoreBackupDialog = true },
                                enabled = !isBackingUp && !isRestoring,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("restore_backup_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isRestoring) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restoring...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Export options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val res = viewModel.exportShopkeeperBackupJson()
                                    res.onSuccess { jsonStr ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, jsonStr)
                                            putExtra(Intent.EXTRA_SUBJECT, "Digital Khata Backup - ${currentUser?.shopName}")
                                            type = "application/json"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Export Backup File"))
                                    }.onFailure { err ->
                                        snackbarMessage = err.message ?: "Export failed"
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("export_backup_json_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export File (JSON)", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val report = buildKhataExportReport(
                                        shopName = currentUser?.shopName ?: "Shop",
                                        customers = customers,
                                        viewModel = viewModel
                                    )
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, report)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Ledger Summary Report"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("export_khata_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Report", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 8. ABOUT SECTION
            item {
                SectionHeader("ABOUT")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AboutClickableRow(Icons.Default.Info, "About Digital Khata") {
                            showSupportDialog = true
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        AboutClickableRow(Icons.Default.PrivacyTip, "Privacy Policy") {
                            showPrivacyPolicyDialog = true
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        AboutClickableRow(Icons.Default.Description, "Terms & Conditions") {
                            showTermsDialog = true
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        AboutClickableRow(Icons.Default.StarRate, "Rate App") {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Digital Khata v1.0.0", Toast.LENGTH_SHORT).show()
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "App Version",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "v1.0.0 (Production)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // DEBUG DIAGNOSTICS
            item {
                com.example.ui.components.UnityAdsDiagnosticsCard()
            }

            // 9. ACCOUNT ACTIONS
            item {
                SectionHeader("ACCOUNT")
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("shop_logout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("shop_delete_account_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KhataRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp), tint = KhataRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", fontWeight = FontWeight.Bold, color = KhataRed)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // DIALOGS

    // 1. Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showEditProfileDialog = false },
            title = { Text("Edit Profile & Shop", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editShopName,
                        onValueChange = { editShopName = it },
                        label = { Text("Shop Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Shopkeeper Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editShopName.isNotBlank() && editName.isNotBlank()) {
                            viewModel.updateUserProfile(
                                name = editName,
                                phone = editPhone,
                                shopName = editShopName
                            ) { res ->
                                if (res.isSuccess) {
                                    showEditProfileDialog = false
                                    snackbarMessage = "Profile updated successfully."
                                } else {
                                    snackbarMessage = res.exceptionOrNull()?.message ?: "Failed to update profile"
                                }
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }, enabled = !isLoading) {
                    Text("Cancel")
                }
            }
        )
    }

    // 1.1 Restore Backup Confirmation Dialog
    if (showRestoreBackupDialog) {
        AlertDialog(
            onDismissRequest = { if (!isRestoring) showRestoreBackupDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Restore Shop Backup?", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Restoring backup will synchronize your customer ledger data with the latest cloud backup. Existing records with identical IDs will be safely merged and updated without creating duplicate entries.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Historical item transactions will remain intact.\n• No duplicate customers or items will be created.\n• Missing records will be restored safely.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoring = true
                        viewModel.restoreShopkeeperCloudBackup { res ->
                            isRestoring = false
                            showRestoreBackupDialog = false
                            res.onSuccess { summary ->
                                lastBackupMeta = viewModel.getShopkeeperLastBackupMetadata()
                                snackbarMessage = summary.message
                            }.onFailure { err ->
                                snackbarMessage = err.message ?: "Restore failed"
                            }
                        }
                    },
                    enabled = !isRestoring,
                    modifier = Modifier.testTag("confirm_restore_backup_button")
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restoring...")
                    } else {
                        Text("Restore Now")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreBackupDialog = false },
                    enabled = !isRestoring
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = {
                            currentPasswordInput = it
                            passwordError = null
                        },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = {
                            newPasswordInput = it
                            passwordError = null
                        },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            passwordError = null
                        },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    passwordError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            currentPasswordInput.isEmpty() -> passwordError = "Please enter current password."
                            newPasswordInput.length < 6 -> passwordError = "New password must be at least 6 characters."
                            newPasswordInput != confirmPasswordInput -> passwordError = "Passwords do not match."
                            else -> {
                                viewModel.changePassword(currentPasswordInput, newPasswordInput) { res ->
                                    if (res.isSuccess) {
                                        showChangePasswordDialog = false
                                        snackbarMessage = "Password changed successfully."
                                    } else {
                                        passwordError = res.exceptionOrNull()?.message ?: "Failed to change password."
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Update Password")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }, enabled = !isLoading) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Are you sure you want to logout?", fontWeight = FontWeight.Bold) },
            text = { Text("You will need to sign in again with your email and password.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showDeleteAccountDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = KhataRed) },
            text = {
                Text(
                    text = "This action cannot be undone. All your shop records, registered customers, and khata entries will be permanently removed.",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount { res ->
                            if (res.isSuccess) {
                                showDeleteAccountDialog = false
                            } else {
                                val err = res.exceptionOrNull()
                                if (err is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ||
                                    err?.message?.contains("recent", ignoreCase = true) == true
                                ) {
                                    showDeleteAccountDialog = false
                                    showReauthDialog = true
                                } else {
                                    snackbarMessage = err?.message ?: "Failed to delete account"
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = KhataRed)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Delete Account")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }, enabled = !isLoading) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5. Reauth Dialog
    if (showReauthDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showReauthDialog = false },
            title = { Text("Confirm Password to Delete", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "For your security, please enter your password to confirm account deletion.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = reauthPasswordInput,
                        onValueChange = {
                            reauthPasswordInput = it
                            reauthError = null
                        },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    reauthError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pass = reauthPasswordInput.trim()
                        if (pass.isEmpty()) {
                            reauthError = "Please enter your password."
                        } else {
                            viewModel.deleteAccount(pass) { res ->
                                if (res.isSuccess) {
                                    showReauthDialog = false
                                } else {
                                    reauthError = res.exceptionOrNull()?.message ?: "Re-authentication failed"
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = KhataRed)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Verify & Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showReauthDialog = false }, enabled = !isLoading) {
                    Text("Cancel")
                }
            }
        )
    }

    // 6. Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digital Khata values your privacy and data security. All ledger entries, customer contact info, and payment records are securely stored on Google Cloud Firestore and encrypted in transit.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "We do not sell or share your business financial records with third parties.",
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 7. Terms & Conditions Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "By using Digital Khata, you agree to maintain accurate records. Customers linked via QR code can view their individual monthly bills and total balance. Shopkeepers retain full administrative ownership of credit entries.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    // 8. Contact Support / About Dialog
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Digital Khata Support", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digital Khata is designed specifically for Pakistani shopkeepers and retail customers.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Need help or have questions? Email: support@digitalkhata.pk",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showSupportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun SettingRowInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun NotificationToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AboutClickableRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

private fun buildKhataExportReport(
    shopName: String,
    customers: List<com.example.data.model.Customer>,
    viewModel: KhataViewModel
): String {
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
    val sb = StringBuilder()
    sb.appendLine("==========================================")
    sb.appendLine("DIGITAL KHATA - SHOP LEDGER REPORT")
    sb.appendLine("Shop: $shopName")
    sb.appendLine("Generated: $dateStr")
    sb.appendLine("Total Customers: ${customers.size}")
    sb.appendLine("==========================================")
    sb.appendLine()

    var grandTotal = 0.0
    customers.forEach { cust ->
        val summary = viewModel.getCustomerBalanceSummary(cust.id)
        grandTotal += summary.totalPayable
        sb.appendLine("• Khata #${cust.khataNumber} - ${cust.name}")
        sb.appendLine("  Phone: ${cust.phone.ifEmpty { "N/A" }}")
        sb.appendLine("  Total Payable: Rs ${summary.totalPayable.toLong()}")
        sb.appendLine("  Current Month (${summary.currentMonthLabel}): Rs ${summary.currentMonthTotal.toLong()}")
        sb.appendLine("  Previous Unsettled: Rs ${summary.previousBalance.toLong()}")
        sb.appendLine("------------------------------------------")
    }

    sb.appendLine()
    sb.appendLine("GRAND TOTAL RECEIVABLE: Rs ${grandTotal.toLong()}")
    sb.appendLine("==========================================")
    return sb.toString()
}
