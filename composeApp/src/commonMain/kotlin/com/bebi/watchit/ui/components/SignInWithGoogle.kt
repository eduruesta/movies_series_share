package com.bebi.watchit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmk.kmpauth.core.KMPAuthInternalApi
import com.mmk.kmpauth.core.di.isAndroidPlatform
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import dev.gitlive.firebase.auth.FirebaseUser
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.google
import moviesseriesshare.composeapp.generated.resources.google_sign_in
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@OptIn(KMPAuthInternalApi::class)
@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val horizontalPadding = if (isAndroidPlatform()) 12.dp else 16.dp
    val iconTextPadding = if (isAndroidPlatform()) 10.dp else 12.dp
    Button(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        onClick = onClick,
        shape = ButtonDefaults.shape,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GoogleIcon()
            Spacer(modifier = Modifier.width(iconTextPadding))
            Text(
                text = stringResource(Res.string.google_sign_in),
                maxLines = 1,
                fontSize = 14.sp
            )
        }

    }
}

@Composable
private fun GoogleIcon() {
    Image(
        modifier = Modifier.size(20.dp),
        painter = painterResource(Res.drawable.google),
        contentDescription = "googleIcon"
    )
}

@Composable
fun GoogleSignIn(onFirebaseResult: (Result<FirebaseUser?>) -> Unit) {
    GoogleButtonUiContainerFirebase(
        onResult = onFirebaseResult, linkAccount = false,
        filterByAuthorizedAccounts = false
    ) {
        GoogleSignInButton(modifier = Modifier, onClick = { onClick() })

    }
}
