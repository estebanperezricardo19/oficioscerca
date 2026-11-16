package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserRole
import com.example.ui.components.OficiosButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundSoft
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.oficiosTextFieldColors
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

@Composable
fun WelcomeScreen(viewModel: OficiosViewModel) {
    LoginScreen(viewModel = viewModel)
}

@Composable
fun LoginScreen(viewModel: OficiosViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()
    val focusManager = LocalFocusManager.current

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Recuperar contraseña",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ingresa el correo de tu cuenta y te enviaremos un enlace para restablecer tu contraseña.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("correo@ejemplo.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = SecondaryTeal)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_password_email_input"),
                        colors = oficiosTextFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendPasswordReset(resetEmail)
                        showResetDialog = false
                    },
                    modifier = Modifier.testTag("reset_password_confirm_btn")
                ) {
                    Text("Enviar enlace", color = SecondaryTeal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        // Subtle brand header atmosphere at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryNavy,
                            SecondaryTeal.copy(alpha = 0.92f),
                            BackgroundSoft
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Top Logo & Brand Identity
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryNavy,
                    border = BorderStroke(1.5.dp, AccentCyan.copy(alpha = 0.55f)),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logotipo de OficiosCerca",
                            modifier = Modifier.size(46.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SurfaceWhite.copy(alpha = 0.92f),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "OficiosCerca",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy,
                        letterSpacing = 0.4.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¡Bienvenido de nuevo!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryNavy,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ingresa para encontrar o gestionar tus servicios",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Main Login Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (authError != null || authMessage != null) {
                                    viewModel.clearAuthErrors()
                                }
                            },
                            enabled = !authLoading,
                            label = { Text("Correo electrónico") },
                            placeholder = { Text("correo@ejemplo.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = SecondaryTeal
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            singleLine = true,
                            colors = oficiosTextFieldColors()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (authError != null || authMessage != null) {
                                    viewModel.clearAuthErrors()
                                }
                            },
                            enabled = !authLoading,
                            label = { Text("Contraseña") },
                            placeholder = { Text("Ingresa tu contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = SecondaryTeal
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showPassword = !showPassword },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("login_toggle_password")
                                ) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = if (showPassword) SecondaryTeal else TextSecondary
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (!authLoading) {
                                        viewModel.login(email, password)
                                    }
                                }
                            ),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            singleLine = true,
                            colors = oficiosTextFieldColors()
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = {
                                    resetEmail = email.trim()
                                    showResetDialog = true
                                },
                                enabled = !authLoading,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .minimumInteractiveComponentSize()
                                    .testTag("forgot_password_btn")
                            ) {
                                Text(
                                    text = "¿Olvidaste tu contraseña?",
                                    fontSize = 13.sp,
                                    color = SecondaryTeal,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = authError != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            authError?.let { error ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_error_banner"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = Color(0xFFB91C1C),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = error,
                                            color = Color(0xFFB91C1C),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = authMessage != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            authMessage?.let { msg ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_message_banner"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE0F7FA),
                                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SecondaryTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = msg,
                                            color = PrimaryNavy,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.login(email, password)
                            },
                            enabled = !authLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryTeal,
                                contentColor = SurfaceWhite,
                                disabledContainerColor = AccentCyan.copy(alpha = 0.65f),
                                disabledContentColor = SurfaceWhite
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 0.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp)
                                .testTag("login_submit_btn")
                        ) {
                            if (authLoading) {
                                CircularProgressIndicator(
                                    color = SurfaceWhite,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Ingresando...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Iniciar sesión",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = authLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = "Autenticando cuenta y verificando tu perfil...",
                                fontSize = 12.sp,
                                color = SecondaryTeal,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿No tienes una cuenta? ",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = {
                            viewModel.clearAuthErrors()
                            viewModel.navigateTo(Screen.Register)
                        },
                        enabled = !authLoading,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("login_go_to_register")
                    ) {
                        Text(
                            text = "Regístrate",
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RegisterScreen(viewModel: OficiosViewModel) {
    val draft by viewModel.registrationDraft.collectAsState()

    var name by remember(draft) { mutableStateOf(draft?.name ?: "") }
    var lastName by remember(draft) { mutableStateOf(draft?.lastName ?: "") }
    var email by remember(draft) { mutableStateOf(draft?.email ?: "") }
    var phone by remember(draft) { mutableStateOf(draft?.phone ?: "") }
    var location by remember(draft) { mutableStateOf(draft?.location ?: "Medellín") }
    var password by remember(draft) { mutableStateOf(draft?.password ?: "") }
    var confirmPassword by remember(draft) { mutableStateOf(draft?.password ?: "") }
    var showPassword by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val emailRegex = remember { Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Crear Cuenta",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryNavy
        )

        Text(
            text = "Únete a la red confiable de OficiosCerca",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        validationError = null
                    },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SecondaryTeal) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_name"),
                    singleLine = true,
                    colors = oficiosTextFieldColors()
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { 
                        lastName = it
                        validationError = null
                    },
                    label = { Text("Apellido") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SecondaryTeal) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_lastname"),
                    singleLine = true,
                    colors = oficiosTextFieldColors()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        validationError = null
                    },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SecondaryTeal) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("reg_email"),
                    singleLine = true,
                    colors = oficiosTextFieldColors()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it
                        validationError = null
                    },
                    label = { Text("Teléfono celular") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SecondaryTeal) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("reg_phone"),
                    singleLine = true,
                    colors = oficiosTextFieldColors()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { 
                        location = it
                        validationError = null
                    },
                    label = { Text("Ciudad / Ubicación") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryTeal) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_location"),
                    singleLine = true,
                    colors = oficiosTextFieldColors()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        validationError = null
                    },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SecondaryTeal) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Mostrar contraseña",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("reg_pass"),
                    singleLine = true,
                    colors = oficiosTextFieldColors()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        validationError = null
                    },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SecondaryTeal) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("reg_pass_conf"),
                    singleLine = true,
                    colors = oficiosTextFieldColors()
                )

                validationError?.let { err ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = err,
                                color = Color(0xFFC62828),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OficiosButton(
                    text = "Continuar a selección de rol",
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedLastName = lastName.trim()
                        val trimmedEmail = email.trim()
                        val trimmedPhone = phone.trim()
                        val trimmedLocation = location.trim().ifBlank { "Medellín" }

                        when {
                            trimmedName.isEmpty() -> {
                                validationError = "Por favor ingresa tu nombre."
                            }
                            trimmedLastName.isEmpty() -> {
                                validationError = "Por favor ingresa tu apellido."
                            }
                            trimmedEmail.isEmpty() -> {
                                validationError = "Por favor ingresa tu correo electrónico."
                            }
                            !emailRegex.matches(trimmedEmail) -> {
                                validationError = "Ingresa un correo electrónico con formato válido (ejemplo: usuario@correo.com)."
                            }
                            trimmedPhone.isEmpty() -> {
                                validationError = "Por favor ingresa tu número telefónico."
                            }
                            password.isEmpty() -> {
                                validationError = "Por favor ingresa una contraseña."
                            }
                            password.length < 6 -> {
                                validationError = "La contraseña debe tener al menos 6 caracteres."
                            }
                            password != confirmPassword -> {
                                validationError = "Las contraseñas no coinciden. Verifícalas e inténtalo de nuevo."
                            }
                            else -> {
                                validationError = null
                                viewModel.setRegistrationDraft(
                                    OficiosViewModel.RegistrationDraft(
                                        name = trimmedName,
                                        lastName = trimmedLastName,
                                        email = trimmedEmail,
                                        phone = trimmedPhone,
                                        location = trimmedLocation,
                                        password = password
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "reg_continue_btn"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes cuenta? ", color = TextSecondary, fontSize = 14.sp)
            Text(
                text = "Inicia sesión",
                color = SecondaryTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { viewModel.navigateTo(Screen.Login) }
                    .padding(4.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RoleSelectScreen(viewModel: OficiosViewModel) {
    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()
    var isWorkerSelected by remember { mutableStateOf(false) }
    var selectedTrade by remember { mutableStateOf(officialTrades.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¿Cómo deseas usar OficiosCerca?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryNavy,
            textAlign = TextAlign.Center
        )

        val draft by viewModel.registrationDraft.collectAsState()
        val isExistingAuthUser = draft == null && com.example.service.AuthService.currentUserId() != null

        Text(
            text = if (isExistingAuthUser) "Tu cuenta está activa. Selecciona tu rol para crear tu perfil y comenzar a usar OficiosCerca." else "Selecciona tu rol para personalizar tu experiencia.",
            fontSize = 14.sp,
            color = if (isExistingAuthUser) SecondaryTeal else TextSecondary,
            fontWeight = if (isExistingAuthUser) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        // Estado de carga mientras se comunica con Supabase
        if (authLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = SecondaryTeal,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Creando tu cuenta y perfil en Supabase...",
                        color = PrimaryNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Mensaje de éxito / confirmación requerida por correo
        authMessage?.let { msg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = msg,
                        color = PrimaryNavy,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    OficiosButton(
                        text = "Ir a Iniciar Sesión",
                        onClick = { viewModel.navigateTo(Screen.Login) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Mensaje de error claro de Supabase
        authError?.let { err ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "No se pudo completar el registro",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = err,
                        color = Color(0xFF424242),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.Register) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Modificar datos de registro",
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Si ya hay un mensaje de confirmación, no mostramos de nuevo las tarjetas de selección
        if (authMessage == null) {
            // Card Cliente
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !authLoading) {
                        viewModel.selectRoleAndProceed(UserRole.CLIENT)
                    }
                    .testTag("role_client_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color(0xFFE0F7FA)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Soy Cliente",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "Necesito contratar profesionales confiables para reparaciones de mi hogar.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SecondaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isWorkerSelected) {
                // Card Trabajador
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !authLoading) {
                            isWorkerSelected = true
                        }
                        .testTag("role_worker_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color(0xFFD6F2F5)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = PrimaryNavy,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Soy Trabajador",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "Ofrezco mis servicios independientes para el hogar y busco nuevos clientes.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SecondaryTeal
                    )
                }
            }
            } else {
                // Panel de selección de oficio específico para el trabajador
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("worker_trade_selection_panel"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFD6F2F5)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                            tint = PrimaryNavy,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Oficio del Trabajador",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                            }
                            TextButton(
                                onClick = { isWorkerSelected = false },
                                enabled = !authLoading
                            ) {
                                Text(
                                    text = "Cambiar",
                                    color = SecondaryTeal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Selecciona tu oficio principal para tu perfil en OficiosCerca:",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        officialTrades.forEach { trade ->
                            val isSelected = selectedTrade.categoryId == trade.categoryId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !authLoading) {
                                        selectedTrade = trade
                                    }
                                    .testTag("trade_option_${trade.profession.lowercase().replace(" ", "_")}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE0F7FA) else BackgroundSoft
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SecondaryTeal) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(28.dp),
                                        shape = CircleShape,
                                        color = if (isSelected) SecondaryTeal else Color(0xFFE0E0E0)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SurfaceWhite,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = trade.profession,
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = PrimaryNavy
                                        )
                                        Text(
                                            text = trade.description,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OficiosButton(
                            text = if (authLoading) "Registrando..." else "Continuar como ${selectedTrade.profession}",
                            onClick = {
                                viewModel.selectRoleAndProceed(
                                    role = UserRole.WORKER,
                                    profession = selectedTrade.profession,
                                    categoryId = selectedTrade.categoryId
                                )
                            },
                            enabled = !authLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_worker_role_button")
                        )
                    }
                }
            }
        }
    }
}

data class OfficialTradeOption(
    val categoryId: String,
    val categoryName: String,
    val profession: String,
    val description: String
)

val officialTrades = listOf(
    OfficialTradeOption(
        categoryId = "330571e3-9665-4088-92da-0f2c98d7f8f5",
        categoryName = "Plomería",
        profession = "Plomero",
        description = "Instalaciones de tuberías, grifería, desagües y filtraciones."
    ),
    OfficialTradeOption(
        categoryId = "d09748d3-40d9-4775-affc-2c12bc6104a9",
        categoryName = "Electricidad",
        profession = "Electricista",
        description = "Reparación de cableado, tableros, tomas e iluminación."
    ),
    OfficialTradeOption(
        categoryId = "df2f0a0d-8f97-45de-8656-3713b07cbfbc",
        categoryName = "Carpintería",
        profession = "Carpintero",
        description = "Muebles a medida, puertas, cocinas y estructuras en madera."
    ),
    OfficialTradeOption(
        categoryId = "199077e9-6042-4f9d-91bc-cb5f7095e903",
        categoryName = "Pintura",
        profession = "Pintor",
        description = "Pintura residencial y comercial, estuco y acabados."
    ),
    OfficialTradeOption(
        categoryId = "5933dda0-ea4d-4b9b-bb10-edd29e3225b0",
        categoryName = "Cerrajería",
        profession = "Cerrajero",
        description = "Apertura de cerraduras, copias de llaves e instalación de cerrojos."
    ),
    OfficialTradeOption(
        categoryId = "7ba3598c-8314-43bd-9811-0738e2da3f71",
        categoryName = "Electrodomésticos",
        profession = "Técnico de electrodomésticos",
        description = "Mantenimiento y arreglo de lavadoras, neveras y electrodomésticos."
    )
)
