package com.news.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*

suspend inline fun <T> Flow<T>.collectIn(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = lifecycleOwner.repeatOnLifecycle(minActiveState) {
    collect {
        action(it)
    }
}

fun formatDate(calendar: Calendar?, formatStr: String): String?{
    return calendar?.let {
        val timeFormat = SimpleDateFormat(formatStr, Locale.getDefault())
        val dt = Date()
        dt.time = calendar.timeInMillis
        return timeFormat.format(dt)
    }
}