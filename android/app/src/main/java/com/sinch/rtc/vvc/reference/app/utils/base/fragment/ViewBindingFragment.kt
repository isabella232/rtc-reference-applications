package com.sinch.rtc.vvc.reference.app.utils.base.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.sinch.rtc.vvc.reference.app.utils.extensions.PermissionRequestResult

abstract class ViewBindingFragment<Binding : ViewBinding>(@LayoutRes val contentLayoutRes: Int) :
    Fragment(contentLayoutRes) {

    companion object {
        const val TAG = "ViewBindingFragment"
    }

    private var sBinding: Binding? = null

    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    private var permissionsResultCallback: (result: PermissionRequestResult) -> Unit = { _ -> }

    val binding: Binding get() = sBinding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedPermissions ->
                Log.d(TAG, "Permissions granted are $grantedPermissions")
                permissionsResultCallback(grantedPermissions)
                permissionsResultCallback = { _ -> }
            }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sBinding = setupBinding(view)
    }

    override fun onDestroyView() {
        sBinding = null
        super.onDestroyView()
    }

    abstract fun setupBinding(root: View): Binding

    fun requestPermissions(
        permissions: List<String>,
        resultCallback: (PermissionRequestResult) -> Unit
    ) {
        this.permissionsResultCallback = resultCallback
        permissionsLauncher.launch(permissions.toTypedArray())
    }

}