package com.goovat.poweralarm

import android.os.Bundle
import android.view.WindowManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class AlarmActivity : FragmentActivity() {

    private var alarmSessionToken: String? = null
    private var authenticationStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmSessionToken = intent.getStringExtra(
            AlarmService.EXTRA_ALARM_SESSION_TOKEN
        )

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    override fun onStart() {
        super.onStart()

        if (
            alarmSessionToken != null &&
            !authenticationStarted
        ) {
            authenticate()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)

        val newToken = intent.getStringExtra(
            AlarmService.EXTRA_ALARM_SESSION_TOKEN
        )

        if (newToken != null) {
            alarmSessionToken = newToken
        }
    }

    private fun authenticate() {
        if (authenticationStarted) {
            return
        }

        authenticationStarted = true

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG

        val biometricManager = BiometricManager.from(this)

        if (
            biometricManager.canAuthenticate(authenticators) !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {
            authenticationStarted = false
            return
        }

        val executor = ContextCompat.getMainExecutor(this)

        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    stopAlarm()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(
                        errorCode,
                        errString
                    )

                    authenticationStarted = false
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    authenticationStarted = false
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Power Alarm")
            .setSubtitle("Authentication required")
            .setDescription(
                "Authenticate to silence the power alarm."
            )
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .setNegativeButtonText("Keep Alarm Active")
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun stopAlarm() {
        val sessionToken = alarmSessionToken
            ?: return

        AlarmService.stopAlarm(
            this,
            sessionToken
        )

        finishAndRemoveTask()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
