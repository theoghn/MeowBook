package com.theo.meowbook.utils

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


open class BaseViewModel<E> : ViewModel(),
    CoroutineScope by MainScope() {

    protected fun <T> DataFlow<T>.execute(
        withLoading: Boolean = true,
        block: suspend DataFlow.ExecuteScope<T>.() -> Unit
    ) = this.executeIn(this@BaseViewModel, withLoading, block)

    override fun onCleared() {
        this.cancel()
        super.onCleared()
    }
}
