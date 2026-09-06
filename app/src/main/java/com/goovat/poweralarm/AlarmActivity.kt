package com.goovat.poweralarm

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class AlarmActivity : FragmentActivity() {

    private var alarmSessionToken: String? = null
    private var authenticationStarted = false
    private var authenticationCompleted = false
    private var biometricPrompt: BiometricPrompt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmSessionToken = intent.getStringExtra(
            AlarmService.EXTRA_ALARM_SESSION_TOKEN
        )

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        createBiometricPrompt()
    }

    override fun onStart() {
        super.onStart()

        if (!authenticationStarted && !authenticationCompleted) {
            authenticate()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val newToken = intent.getStringExtra(
            AlarmService.EXTRA_ALARM_SESSION_TOKEN
        )

        if (newToken != null && newToken != alarmSessionToken) {
            alarmSessionToken = newToken
            authenticationStarted = false
            authenticationCompleted = false
            authenticate()
        }
    }

    private fun createBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    authenticationStarted = false
                    authenticationCompleted = true
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
                }
            }
        )
    }

    private fun authenticate() {
        if (authenticationStarted || authenticationCompleted) {
            return
        }

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG

        val biometricManager = BiometricManager.from(this)

        if (
            biometricManager.canAuthenticate(authenticators) !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {
            return
        }

        val prompt = biometricPrompt
            ?: return

        authenticationStarted = true

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
