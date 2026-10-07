package com.example.ui.customer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.ConnectedKhata
import com.example.data.model.CustomerTab
import com.example.data.model.ThemeMode
import com.example.ui.theme.KhataRed
import com.example.ui.viewmodel.KhataViewModel
import com.example.utils.CurrencyFormatter
import com.example.utils.HapticManager
import com.example.utils.NotificationPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSettingsScreen(
    viewModel: KhataViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val connectedKhatas by viewModel.connectedKhatas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val vibrationEnabled by HapticManager.vibrationEnabled.collectAsState()

    // Dialog States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showReauthDialog by remember { mutableStateOf(false) }
    var showRestoreBackupDialog by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var lastBackupMeta by remember { mutableStateOf(viewModel.getCustomerLastBackupMetadata()) }
    var khataToRemove by remember { mutableStateOf<ConnectedKhata?>(null) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    // Inputs
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    // Password Inputs
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

    // Notification Preferences
    var notifNewKhataEntry by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_CUST_NEW_KHATA_ENTRY))
    }
    var notifPaymentUpdates by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_CUST_PAYMENT_UPDATES))
    }
    var notifPaymentAccepted by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_CUST_PAYMENT_ACCEPTED))
    }
    var notifPaymentRejected by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_CUST_PAYMENT_REJECTED))
    }
    var notifPrevBalance by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_CUST_PREV_BALANCE))
    }
    var notifMonthlyReminder by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, NotificationPreferences.KEY_CUST_MONTHLY_REMINDER))
    }

    Scaffold(
        modifier = modifier.testTag("customer_settings_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Customer Settings",
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
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.name ?: "Customer",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${connectedKhatas.size} Connected Shop(s)",
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
                                showEditProfileDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("cust_edit_profile_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 2. MY KHATAS (CONNECTED SHOPS)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("MY KHATAS (${connectedKhatas.size})")
                    TextButton(
                        onClick = { viewModel.setCustomerTab(CustomerTab.HOME) }
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Khata", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (connectedKhatas.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Connected Shops Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You can connect to multiple shops using this single account. Ask any shopkeeper for their QR code to link your khata.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(connectedKhatas, key = { it.connectionId }) { conn ->
                    val summary = viewModel.getCustomerBalanceSummary(conn.customerId, conn.shopId)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conn.shopName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Shopkeeper: ${conn.shopOwnerName} • Khata #${conn.khataNumber}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = CurrencyFormatter.format(summary.totalPayable),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { khataToRemove = conn },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove Connection", fontSize = 12.sp)
                                }
                            }
                        }
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
                        NotificationToggleRow("New Khata Entry", "Alert when a shop adds an item to your bill", notifNewKhataEntry) {
                            notifNewKhataEntry = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_CUST_NEW_KHATA_ENTRY, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Payment Request Updates", "Notifications on your submitted payments", notifPaymentUpdates) {
                            notifPaymentUpdates = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_CUST_PAYMENT_UPDATES, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Payment Accepted", "Confirmation when shopkeeper accepts payment", notifPaymentAccepted) {
                            notifPaymentAccepted = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_CUST_PAYMENT_ACCEPTED, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Payment Rejected", "Alert if payment request was declined", notifPaymentRejected) {
                            notifPaymentRejected = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_CUST_PAYMENT_REJECTED, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Previous Balance Reminder", "Periodic summary of unsettled balances", notifPrevBalance) {
                            notifPrevBalance = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_CUST_PREV_BALANCE, it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        NotificationToggleRow("Monthly Reminder", "Remind to check monthly bills across shops", notifMonthlyReminder) {
                            notifMonthlyReminder = it
                            NotificationPreferences.setEnabled(context, NotificationPreferences.KEY_CUST_MONTHLY_REMINDER, it)
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
                                        .testTag("cust_theme_button_${mode.name.lowercase()}"),
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
                            modifier = Modifier.testTag("cust_vibration_switch")
                        )
                    }
                    if (vibrationEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { HapticManager.testVibration(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cust_test_vibration_btn"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Vibration", fontSize = 12.sp)
                        }
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

            // 6.1. BACKUP & RESTORE
            item {
                SectionHeader("BACKUP & RESTORE")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("customer_backup_card")
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
                                    text = "Account Cloud Backup",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lastBackupMeta != null)
                                        "Last synced: ${lastBackupMeta!!.dateFormatted} • ${connectedKhatas.size} Connected Shops"
                                    else "No backup saved yet.",
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isBackingUp = true
                                    viewModel.createCustomerCloudBackup { res ->
                                        isBackingUp = false
                                        res.onSuccess { summary ->
                                            lastBackupMeta = viewModel.getCustomerLastBackupMetadata()
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
                                    .testTag("customer_create_backup_btn"),
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
                                    Text("Backup Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { showRestoreBackupDialog = true },
                                enabled = !isBackingUp && !isRestoring,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("customer_restore_backup_btn"),
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
                    }
                }
            }

            // 7. ABOUT SECTION
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

            // GOOGLE ADMOB TEST IDS & DIAGNOSTICS
            item {
                com.example.ui.components.AdMobDiagnosticsCard()
            }

            // 8. ACCOUNT ACTIONS
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
                            .testTag("cust_logout_button"),
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
                            .testTag("cust_delete_account_button"),
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
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Customer Name") },
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
                        if (editName.isNotBlank()) {
                            viewModel.updateUserProfile(
                                name = editName,
                                phone = editPhone
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

    // 2. Remove Khata Connection Dialog
    if (khataToRemove != null) {
        val targetKhata = khataToRemove!!
        AlertDialog(
            onDismissRequest = { if (!isLoading) khataToRemove = null },
            title = { Text("Remove Khata Connection?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to disconnect from \"${targetKhata.shopName}\"?\n\nNote: This will only remove the connection from your phone. The shopkeeper's original records, bills, and purchase history remain completely safe.",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeKhataConnection(targetKhata.connectionId) { res ->
                            khataToRemove = null
                            if (res.isSuccess) {
                                snackbarMessage = "Khata connection removed from your account."
                            } else {
                                snackbarMessage = res.exceptionOrNull()?.message ?: "Failed to remove connection"
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Remove")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { khataToRemove = null }, enabled = !isLoading) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Backup Dialog (Customer)
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
                Text("Restore Account Backup?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Restoring backup will synchronize your saved shop connections and preferences from the cloud. Your connected shop khata records will be re-linked safely.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoring = true
                        viewModel.restoreCustomerCloudBackup { res ->
                            isRestoring = false
                            showRestoreBackupDialog = false
                            res.onSuccess { summary ->
                                lastBackupMeta = viewModel.getCustomerLastBackupMetadata()
                                snackbarMessage = summary.message
                            }.onFailure { err ->
                                snackbarMessage = err.message ?: "Restore failed"
                            }
                        }
                    },
                    enabled = !isRestoring,
                    modifier = Modifier.testTag("confirm_cust_restore_button")
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

    // 3. Change Password Dialog
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

    // 4. Logout Dialog
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

    // 5. Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showDeleteAccountDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = KhataRed) },
            text = {
                Text(
                    text = "This action will permanently remove your login account. Note: Shopkeeper-owned khata records and historical ledgers will not be deleted.",
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

    // 6. Reauth Dialog
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

    // 7. Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digital Khata protects your personal information. As a customer, your connected bills are encrypted and visible only to you and the respective shopkeepers you connected with.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "We never share or monetize your private credit or payment data.",
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

    // 8. Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "By using Digital Khata, you agree to review purchase entries and submit payment requests faithfully. Connecting a shop grants you view-only access to your bill.",
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

    // 9. Support Dialog
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Digital Khata Support", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digital Khata lets you manage credit khatas across multiple shops with one phone.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Questions or feedback? Email: support@digitalkhata.pk",
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
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun NotificationToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
private fun AboutClickableRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
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
