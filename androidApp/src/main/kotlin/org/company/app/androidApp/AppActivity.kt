package org.company.app.androidApp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.firebase_auth.AndroidKFirebaseAuth
import io.github.firebase_core.AndroidKFirebaseCore
import io.github.kmmcrypto.AndroidKMMCrypto
import io.github.sign_in_with_google.AndroidGoogleSignIn
import io.gituhb.demo.App


class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AndroidKFirebaseCore.initialization(this)
        AndroidKFirebaseAuth.init(
            this,
            "204571788770-63q1akee6h2fgjdkafepa8jhouqh2csv.apps.googleusercontent.com"
        )
        AndroidGoogleSignIn.initialization(this)
        AndroidKMMCrypto.init("key0")

        setContent { App() }
    }
}
