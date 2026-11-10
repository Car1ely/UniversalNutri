package uz.universalnutri.app.ui.screens

import androidx.compose.ui.res.stringResource
import uz.universalnutri.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.universalnutri.app.ui.ShopViewModel
import uz.universalnutri.app.ui.goalLabel
import uz.universalnutri.app.ui.localizedError
import uz.universalnutri.app.ui.paymentLabel
import uz.universalnutri.app.ui.theme.BrandGradient

@Composable
fun LoginScreen(vm: ShopViewModel, onLoggedIn: () -> Unit, onRegisterClick: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    AuthLayout(title = stringResource(R.string.welcome_back), subtitle = stringResource(R.string.login_subtitle)) {
        AuthField(email, { email = it; error = null }, stringResource(R.string.email), Icons.Filled.Email, KeyboardType.Email)
        PasswordField(password, { password = it; error = null })
        error?.let { Text(localizedError(it) ?: it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        Button(
            onClick = {
                loading = true
                vm.login(email, password) { err ->
                    loading = false
                    error = err
                    if (err == null) onLoggedIn()
                }
            },
            enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text(stringResource(R.string.log_in), fontSize = 16.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.no_account), color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onRegisterClick) { Text(stringResource(R.string.register), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun RegisterScreen(vm: ShopViewModel, onRegistered: () -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    AuthLayout(title = stringResource(R.string.create_account), subtitle = stringResource(R.string.register_subtitle)) {
        AuthField(name, { name = it; error = null }, stringResource(R.string.full_name), Icons.Filled.Person, KeyboardType.Text)
        AuthField(email, { email = it; error = null }, stringResource(R.string.email), Icons.Filled.Email, KeyboardType.Email)
        PasswordField(password, { password = it; error = null }, label = stringResource(R.string.password_rules))
        error?.let { Text(localizedError(it) ?: it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        Button(
            onClick = {
                loading = true
                vm.register(name, email, password) { err ->
                    loading = false
                    error = err
                    if (err == null) onRegistered()
                }
            },
            enabled = !loading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text(stringResource(R.string.create_account), fontSize = 16.sp)
        }
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.have_account))
        }
    }
}

/** Orange hero with the logo on top, white sheet with the form below. */
@Composable
private fun AuthLayout(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(BrandGradient)
                .statusBarsPadding()
                .padding(top = 40.dp, bottom = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Logo(size = 76)
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.app_name), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.brand_tagline), color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().offset(y = (-32).dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                content()
            }
        }
    }
}

/** App logo: lightning bolt in a white rounded square. */
@Composable
fun Logo(size: Int) {
    Surface(shape = RoundedCornerShape((size * 0.3).dp), color = Color.White, shadowElevation = 6.dp,
        modifier = Modifier.size(size.dp)) {
        Box(contentAlignment = Alignment.Center) { Text("⚡", fontSize = (size * 0.5).sp) }
    }
}

@Composable
private fun AuthField(
    value: String, onChange: (String) -> Unit, label: String, icon: ImageVector, type: KeyboardType
) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = type, imeAction = ImeAction.Next)
    )
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, label: String = stringResource(R.string.password)) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) stringResource(R.string.hide_password) else stringResource(R.string.show_password))
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
    )
}
