package com.sinch.rtc.vvc.reference.app.domain.calls

import android.content.Context
import com.sinch.rtc.vvc.reference.app.R

enum class CallType {
    AppToPhone,
    AppToAppAudio,
    AppToAppVideo,
    AppToSip;
}

fun CallType.newCallLabel(context: Context): String {
    val resource = when (this) {
        CallType.AppToPhone -> R.string.call_app_to_phone
        CallType.AppToAppAudio -> R.string.call_app_to_app_audio
        CallType.AppToAppVideo -> R.string.call_app_to_app_video
        CallType.AppToSip -> R.string.call_app_to_sip
    }
    return context.getString(resource)

}