package com.goovat.poweralarm

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class AlarmActivity : FragmentActivity() {

    private var authenticationStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        authenticate()
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

                    // Authentication was cancelled, rejected,
                    // or unavailable. The alarm remains active.
                    authenticationStarted = false
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    // A biometric was presented but did not match.
                    // The alarm remains active.
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
        AlarmService.stopAlarm(this)
        finishAndRemoveTask()
    }

    override fun onBackPressed() {
        // Back must never silence the alarm.
        moveTaskToBack(true)
    }
}
