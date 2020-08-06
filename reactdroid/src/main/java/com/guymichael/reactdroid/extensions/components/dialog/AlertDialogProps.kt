package com.guymichael.reactdroid.extensions.components.dialog

import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.OwnProps

data class AlertDialogProps<CUSTOM_CONTENT_PROPS : OwnProps>(
    override val shown: Boolean
    , val title: CharSequence?
    , val message: CharSequence?
    , val okBtn: Pair<CharSequence, ((props: AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> APromise<*>?)?>?
    , val cancelBtn: Pair<CharSequence, ((props: AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> APromise<*>?)?>? = null
    , val neutralBtn: Pair<CharSequence, ((props: AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> APromise<*>?)?>? = null
//    , val cancelable: Boolean = false AlertDialog internal method doesn't check for newCancelable != prev
    , override val customContentProps: CUSTOM_CONTENT_PROPS? = null
) : BaseAlertDialogProps<CUSTOM_CONTENT_PROPS>(shown, customContentProps) {

    override fun getAllMembers() = listOf(
        shown, title, message, /*cancelable,*/ okBtn?.first, cancelBtn?.first, neutralBtn?.first
        , customContentProps
    )
}