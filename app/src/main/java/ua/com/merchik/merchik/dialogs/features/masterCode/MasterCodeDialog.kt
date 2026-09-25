package ua.com.merchik.merchik.dialogs.features.masterCode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.features.calendar.MerchikDatePickerDialog
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val MASTER_CODE_PASSWORD = "1234"
private const val ACCESS_PREFERENCES = "master_code_access"
private const val FAILED_ATTEMPTS_KEY = "failed_attempts"
private const val BLOCKED_UNTIL_KEY = "blocked_until_ms"

@Composable
fun MasterCodeDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val preferences = remember(context) {
        context.applicationContext.getSharedPreferences(ACCESS_PREFERENCES, Context.MODE_PRIVATE)
    }

    fun readAccessState(nowMs: Long): MasterCodeAccessState = MasterCodeAccessState(
        failedAttempts = preferences.getInt(FAILED_ATTEMPTS_KEY, 0),
        blockedUntilMs = preferences.getLong(BLOCKED_UNTIL_KEY, 0L)
    ).refreshed(nowMs)

    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var accessState by remember(preferences) { mutableStateOf(readAccessState(nowMs)) }
    var authorized by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT) }
    var selectedDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordRejected by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    var masterCode by remember { mutableStateOf<String?>(null) }
    val blocked = accessState.isBlocked(nowMs)
    val shape = RoundedCornerShape(8.dp)
    val borderColor = colorResource(R.color.borderContextMenu)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = colorResource(R.color.orange),
        unfocusedBorderColor = borderColor,
        focusedLabelColor = Color.DarkGray,
        unfocusedLabelColor = Color.DarkGray,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color(0xFFF2F2F2),
        disabledBorderColor = borderColor.copy(alpha = 0.5f),
        disabledLabelColor = Color.Gray
    )

    fun saveAccessState(state: MasterCodeAccessState) {
        accessState = state
        preferences.edit()
            .putInt(FAILED_ATTEMPTS_KEY, state.failedAttempts)
            .putLong(BLOCKED_UNTIL_KEY, state.blockedUntilMs)
            .apply()
    }

    LaunchedEffect(accessState.blockedUntilMs) {
        if (accessState.blockedUntilMs == 0L) return@LaunchedEffect
        while (true) {
            nowMs = System.currentTimeMillis()
            if (!accessState.isBlocked(nowMs)) {
                saveAccessState(accessState.refreshed(nowMs))
                passwordRejected = false
                break
            }
            delay(1000L)
        }
    }

    fun authorize() {
        if (authorized) return
        nowMs = System.currentTimeMillis()
        // Re-read before checking the password so reopening another dialog cannot bypass the lock.
        val currentState = readAccessState(nowMs)
        accessState = currentState
        if (currentState.isBlocked(nowMs)) return
        if (password != MASTER_CODE_PASSWORD) {
            val failedState = currentState.afterFailure(nowMs)
            saveAccessState(failedState)
            passwordRejected = true
            if (failedState.isBlocked(nowMs)) {
                password = ""
                passwordVisible = false
                focusManager.clearFocus()
            }
            return
        }
        saveAccessState(MasterCodeAccessState())
        password = ""
        passwordVisible = false
        passwordRejected = false
        error = null
        authorized = true
        focusManager.clearFocus()
    }

    fun generate() {
        if (!authorized) return
        if (!MasterCodeGenerator.isDateAllowed(selectedDate, LocalDate.now())) {
            masterCode = null
            error = R.string.master_code_invalid_date
            return
        }
        try {
            masterCode = MasterCodeGenerator.generate(selectedDate)
            error = null
            focusManager.clearFocus()
        } catch (_: IllegalStateException) {
            error = R.string.master_code_generation_error
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.9f)
                .padding(vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    onClick = onDismiss
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().clip(shape).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val generatedCode = masterCode
                Text(
                    text = stringResource(R.string.master_code_title),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                if (!authorized) {
                    Text(
                        text = stringResource(R.string.master_code_access_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordRejected = false
                            error = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.master_code_password)) },
                        enabled = !blocked,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconToggleButton(
                                checked = passwordVisible,
                                onCheckedChange = { passwordVisible = it },
                                enabled = !blocked
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (passwordVisible) com.google.android.material.R.drawable.design_ic_visibility_off
                                        else com.google.android.material.R.drawable.design_ic_visibility
                                    ),
                                    contentDescription = stringResource(
                                        if (passwordVisible) R.string.master_code_hide_password else R.string.master_code_show_password
                                    ),
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Black.copy(alpha = if (blocked) 0.38f else 0.54f)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { authorize() }),
                        singleLine = true,
                        isError = passwordRejected,
                        shape = shape,
                        colors = fieldColors
                    )
                    if (blocked) {
                        val remainingSeconds = accessState.remainingSeconds(nowMs)
                        val remainingTime = String.format(Locale.ROOT, "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)
                        Text(
                            text = stringResource(R.string.master_code_locked, remainingTime),
                            color = colorResource(R.color.red_error),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (accessState.failedAttempts > 0) {
                        Text(
                            text = stringResource(
                                if (passwordRejected) R.string.master_code_invalid_password_attempts else R.string.master_code_attempts_remaining,
                                accessState.attemptsRemaining
                            ),
                            color = if (passwordRejected) colorResource(R.color.red_error) else Color.DarkGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.master_code_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = stringResource(R.string.master_code_date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            .clip(shape).border(1.dp, borderColor, shape)
                            .clickable(role = Role.Button) {
                                focusManager.clearFocus()
                                showDatePicker = true
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = selectedDate.format(formatter),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                    }
                    OutlinedTextField(
                        value = generatedCode.orEmpty(),
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.master_code_title)) },
                        enabled = generatedCode != null,
                        readOnly = true,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                        trailingIcon = {
                            if (generatedCode != null) IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.master_code_title), generatedCode))
                                Toast.makeText(context, R.string.master_code_copied, Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(painterResource(R.drawable.ic_master_code_copy), contentDescription = stringResource(R.string.master_code_copy), tint = Color.DarkGray)
                            }
                        },
                        shape = shape,
                        colors = fieldColors
                    )
                }
                error?.let { Text(stringResource(it), color = colorResource(R.color.red_error), style = MaterialTheme.typography.bodyMedium) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (authorized) Arrangement.spacedBy(8.dp) else Arrangement.End
                ) {
                    if (authorized) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = shape,
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue), contentColor = Color.White)
                        ) {
                            Text(stringResource(R.string.master_code_close))
                        }
                        Button(
                            onClick = { generate() },
                            modifier = Modifier.weight(1f),
                            shape = shape,
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange), contentColor = Color.White)
                        ) {
                            Text(stringResource(R.string.master_code_create))
                        }
                    } else {
                        Button(
                            onClick = { if (blocked) onDismiss() else authorize() },
                            modifier = Modifier.widthIn(min = 120.dp),
                            shape = shape,
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange), contentColor = Color.White)
                        ) {
                            Text(stringResource(if (blocked) R.string.master_code_close else R.string.ok))
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker && authorized) {
        MerchikDatePickerDialog(
            visible = true,
            initialDate = selectedDate,
            title = stringResource(R.string.master_code_date),
            allowedDateValidator = { MasterCodeGenerator.isDateAllowed(it, LocalDate.now()) },
            onDateSelected = {
                if (selectedDate != it) {
                    selectedDate = it
                    masterCode = null
                }
                error = null
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
