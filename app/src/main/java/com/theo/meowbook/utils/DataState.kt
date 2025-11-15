package com.theo.meowbook.utils

import androidx.compose.runtime.Immutable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed interface DataState<out T> {
    data object Uninitialized : DataState<Nothing>
    data object Loading : DataState<Nothing>

    @Immutable
    data class Success<out T>(val data: T) : DataState<T>

    @Immutable
    data class Failed(val throwable: Throwable) : DataState<Nothing>
}

val <T> DataState<T>.data get() = (this as DataState.Success<T>).data
val <T> DataState<T>.dataOrNull get() = (this as? DataState.Success<T>)?.data
val <T> DataState<T>.failure get() = (this as DataState.Failed).throwable
val <T> DataState<T>.failureOrNull get() = (this as? DataState.Failed)?.throwable
val <T> DataState<T>.isLoading get() = this is DataState.Loading
val <T> DataState<T>.isFailed get() = this is DataState.Failed
val <T> DataState<T>.isSuccess get() = this is DataState.Success


@OptIn(ExperimentalContracts::class)
inline fun <T> DataState<T>.select(
    onUninitialized: () -> Unit = {},
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onFailed: (Throwable) -> Unit = {},
) {
    contract {
        callsInPlace(onUninitialized, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onLoading, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailed, InvocationKind.AT_MOST_ONCE)
    }

    when (this) {
        DataState.Uninitialized -> onUninitialized()
        DataState.Loading -> onLoading()
        is DataState.Success -> onSuccess(data)
        is DataState.Failed -> onFailed(throwable)
    }
}

inline fun <T> DataState<T>.ifSuccess(block: (T) -> Unit) {
    if (this is DataState.Success<T>) {
        block(this.data)
    }
}

inline fun <T, R> DataState<T>.ifSuccessOrDefault(defaultValue: R, block: (data: T) -> R): R {
    return if (this is DataState.Success) {
        block(data)
    } else {
        defaultValue
    }
}

inline fun <T> DataState<T>.ifFailed(block: (error: Throwable) -> Unit) {
    if (this is DataState.Failed) {
        block(throwable)
    }
}

inline fun <T> DataState<T>.ifLoading(block: () -> Unit) {
    if (this is DataState.Loading) {
        block()
    }
}

