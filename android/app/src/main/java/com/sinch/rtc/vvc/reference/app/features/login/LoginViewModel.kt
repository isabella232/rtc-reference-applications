package com.sinch.rtc.vvc.reference.app.features.login

import SingleLiveEvent
import android.app.Application
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sinch.android.rtc.*
import com.sinch.rtc.vvc.reference.app.R
import com.sinch.rtc.vvc.reference.app.application.Constants
import com.sinch.rtc.vvc.reference.app.utils.jwt.JWTFetcher
import com.sinch.rtc.vvc.reference.app.utils.jwt.getString
import java.util.*

class LoginViewModel(application: Application, private val jwtFetcher: JWTFetcher) :
    AndroidViewModel(application), UserRegistrationCallback, PushTokenRegistrationCallback {

    private val viewModelState: MutableLiveData<LoginViewState> = MutableLiveData(Idle)
    private var loggingTimeoutHandler: Handler? = null

    val errorMessages: SingleLiveEvent<String> = SingleLiveEvent()
    val navigationEvents: SingleLiveEvent<LoginNavigationEvent> = SingleLiveEvent()

    val isLoginButtonEnabled: LiveData<Boolean>
        get() =
            Transformations.map(viewModelState) { viewState ->
                return@map when (viewState) {
                    Idle -> true
                    else -> false
                }
            }

    companion object {
        const val TAG = "LoginViewModel"
        const val LOGGING_TIMEOUT_MS = 10000L
    }

    fun onLoginClicked(username: String) {
        Log.d(TAG, "Login clicked with username $username")
        if (SinchHelpers.isGooglePlayServicesAvailable(getApplication())) {
            login(username)
        } else {
            errorMessages.postValue(getString(R.string.play_services_not_available_error_message))
        }
    }

    private fun login(username: String) {
        viewModelState.value = Logging(
            username,
            isUserRegistered = false,
            isPushTokenRegistered = false
        )
        Sinch.getUserControllerBuilder()
            .context(getApplication())
            .applicationKey(Constants.APP_KEY)
            .userId(username)
            .environmentHost(Constants.ENVIRONMENT)
            .build().registerUser(
                this,
                this
            )
        initLoggingTimeoutTimer()
    }

    override fun onPushTokenRegistered() {
        Log.d(TAG, "onUserRegistered")
        ifLoggingIn {
            val newState = Logging(it.username, it.isUserRegistered, true)
            viewModelState.value = newState
            checkIfRegistrationComplete(newState)
        }
    }

    override fun onUserRegistered() {
        Log.d(TAG, "onUserRegistered")
        ifLoggingIn {
            val newState = Logging(it.username, true, it.isPushTokenRegistered)
            viewModelState.value = newState
            checkIfRegistrationComplete(newState)
        }
    }

    override fun onCredentialsRequired(clientRegistration: ClientRegistration) {
        Log.d(TAG, "onCredentialsRequired $clientRegistration")
        val currentState = viewModelState.value
        if (currentState is Logging) {
            jwtFetcher.acquireJWT(Constants.APP_KEY, currentState.username) { jwt ->
                clientRegistration.register(jwt)
            }
        }
    }

    override fun onUserRegistrationFailed(error: SinchError?) {
        Log.d(TAG, "onUserRegistrationFailed $error")
        resetToIdleWithErrorMessage("${error?.message.orEmpty()}\nExtras: ${error?.extras}")
    }

    override fun onPushTokenRegistrationFailed(error: SinchError?) {
        Log.d(TAG, "onPushTokenRegistrationFailed $error")
        resetToIdleWithErrorMessage("${error?.message.orEmpty()}\nExtras: ${error?.extras}")
    }

    private fun ifLoggingIn(f: (logginState: Logging) -> Unit) {
        val currentState = viewModelState.value
        if (currentState is Logging) {
            f(currentState)
        }
    }

    private fun checkIfRegistrationComplete(state: Logging) {
        if (state.isLoggingComplete) {
            cancelLoggingTimeoutTimer()
            viewModelState.value = Idle
            navigationEvents.postValue(Dashboard)
        }
    }

    private fun initLoggingTimeoutTimer() {
        loggingTimeoutHandler = Handler().apply {
            postDelayed({
                resetToIdleWithErrorMessage(getString(R.string.logging_timeout_error_message))
            }, LOGGING_TIMEOUT_MS)
        }
    }

    private fun resetToIdleWithErrorMessage(message: String) {
        cancelLoggingTimeoutTimer()
        viewModelState.value = Idle
        errorMessages.postValue(message)
    }

    private fun cancelLoggingTimeoutTimer() {
        loggingTimeoutHandler?.removeCallbacksAndMessages(null)
        loggingTimeoutHandler = null
    }

}