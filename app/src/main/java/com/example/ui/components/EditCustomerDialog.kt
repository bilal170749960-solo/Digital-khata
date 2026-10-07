package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.viewmodel.KhataViewModel

@Composable
fun EditCustomerDialog(
    customer: Customer,
    viewModel: KhataViewModel,
    onDismiss: () -> Unit,
    onCustomerUpdated: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var khataNumber by remember { mutableStateOf(customer.khataNumber) }
    var phone by remember { mutableStateOf(customer.phone) }
    var address by remember { mutableStateOf(customer.address) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        modifier = Modifier.testTag("edit_customer_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Edit Customer Details",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Update profile details for this khata ledger. Khata number must remain unique.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Customer Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_customer_name_field")
                )

                // Khata Number
                OutlinedTextField(
                    value = khataNumber,
                    onValueChange = {
                        khataNumber = it
                        errorMessage = null
                    },
                    label = { Text("Khata Number *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_customer_khata_field")
                )

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_customer_phone_field")
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_customer_address_field")
                )

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isBlank()) {
                        errorMessage = "Customer name is required."
                        return@Button
                    }
                    if (khataNumber.trim().isBlank()) {
                        errorMessage = "Khata number is required."
                        return@Button
                    }

                    isSaving = true
                    viewModel.updateCustomer(
                        customerId = customer.id,
                        name = name.trim(),
                        khataNumber = khataNumber.trim(),
                        phone = phone.trim(),
                        address = address.trim()
                    ) { result ->
                        isSaving = false
                        result.onSuccess { updated ->
                            onCustomerUpdated(updated)
                            onDismiss()
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Failed to update customer"
                        }
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_edit_customer_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Saving...")
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving,
                modifier = Modifier.testTag("cancel_edit_customer_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
