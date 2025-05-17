package com.vlamik.spacex.common.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Sealed class for representing text that can be a dynamic String or a String resource.
 * Allows deferring the resolution of String resources to the UI layer, making the ViewModel independent of Context.
 */
sealed class UiText {

    /**
     * Represents a dynamic String.
     * @param value The actual String value.
     */
    data class DynamicString(val value: String) : UiText()

    /**
     * Represents a String resource.
     * @param resId The String resource ID.
     * @param args Arguments for formatting the String resource.
     */
    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText()

    /**
     * Resolves the UiText to an actual String using the provided Context.
     * @param context The Context needed to access String resources.
     * @return The resolved String.
     */
    fun asString(context: Context): String = when (this) {
        is DynamicString -> value

        is StringResource ->
            if (args.isEmpty()) {
                context.getString(resId)
            } else {
                context.getString(resId, *args.toTypedArray())
            }
    }

    companion object {
        /**
         * Creates a DynamicString instance.
         * @param value The actual String value.
         * @return A UiText.DynamicString instance.
         */
        fun dynamic(value: String): UiText =
            DynamicString(value)

        /**
         * Creates a StringResource instance.
         * @param resId The String resource ID.
         * @param args Arguments for formatting the String resource.
         * @return A UiText.StringResource instance.
         */
        fun from(@StringRes resId: Int, vararg args: Any): UiText =
            StringResource(resId, args.toList())
    }
}

/**
 * Compose extension property to resolve UiText to a String within a Composable function.
 * @return The resolved String.
 */
@Composable
fun UiText.asString(): String {
    val context = LocalContext.current
    return this.asString(context)
}
