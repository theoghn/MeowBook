package com.theo.meowbook.utils

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReference

@DslMarker
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ExecuteDsl

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class DataFlow<T>(initialState: DataState<T>) : StateFlow<DataState<T>> {
    interface Emitter<T> {
        suspend fun setValue(fn: suspend (prevState: DataState<T>) -> T)
        suspend fun replaceValue(fn: suspend (prevValue: T) -> T)
    }

    @ExecuteDsl
    class ExecuteScope<T>(
        scope: CoroutineScope,
        private val emitter: Emitter<T>,
    ) : CoroutineScope by scope {
        suspend fun setValue(fn: suspend (prevState: DataState<T>) -> T) = emitter.setValue(fn)
        suspend fun setValue(value: T) = emitter.setValue { value }
        suspend fun replaceValue(fn: suspend (prevValue: T) -> T) = emitter.replaceValue(fn)
    }

    companion object {
        fun <T> uninitialized(): DataFlow<T> = DataFlow(DataState.Uninitialized)
        fun <T> loading(): DataFlow<T> = DataFlow(DataState.Loading)
        fun <T> success(data: T): DataFlow<T> = DataFlow(DataState.Success(data))
        fun <T> failed(e: Throwable): DataFlow<T> = DataFlow(DataState.Failed(e))
    }

    private val stateFlow = MutableStateFlow(initialState)
    private var activeJob: AtomicReference<Job?> = AtomicReference(null)

    override val replayCache: List<DataState<T>>
        get() = stateFlow.replayCache

    override val value: DataState<T>
        get() = stateFlow.value

    override suspend fun collect(collector: FlowCollector<DataState<T>>): Nothing {
        stateFlow.collect(collector)
    }

    private fun update(value: DataState<T>) {
        stateFlow.update { value }
    }

    fun reset() {
        activeJob.getAndUpdate { null }?.cancel()
        update(DataState.Uninitialized)
    }

    suspend fun awaitResult(): Result<T> {
        return filter { it is DataState.Success || it is DataState.Failed }
            .map {
                when (it) {
                    is DataState.Success -> Result.success(it.data)
                    is DataState.Failed -> Result.failure(it.throwable)
                    else -> throw AssertionError()
                }
            }.first()
    }

    fun executeIn(
        scope: CoroutineScope,
        withLoading: Boolean,
        block: suspend ExecuteScope<T>.() -> Unit
    ): Job {
        activeJob.getAndUpdate { null }?.cancel()

        val job = scope.launch {
            var prevState = value

            val emitter = object : Emitter<T> {
                val mutex = Mutex()

                override suspend fun setValue(fn: suspend (prevState: DataState<T>) -> T) {
                    coroutineContext.ensureActive()
                    mutex.withLock {
                        val success = DataState.Success(fn(prevState))
                        coroutineContext.ensureActive()
                        update(success)
                        prevState = success
                    }
                }

                override suspend fun replaceValue(fn: suspend (prevValue: T) -> T) {
                    coroutineContext.ensureActive()
                    mutex.withLock {
                        (prevState as? DataState.Success<T>)?.apply {
                            val success = DataState.Success(fn(this.data))
                            coroutineContext.ensureActive()
                            update(success)
                            prevState = success
                        }
                    }
                }

            }

            val executeScope = ExecuteScope(this, emitter)

            if (withLoading) {
                update(DataState.Loading)
            }
            try {
                executeScope.block()
            } catch (e: Throwable) {
                Log.e("DataFlow", e.message.orEmpty())

                if (e is CancellationException) {
                    throw e
                }

                if (coroutineContext.isActive) {
                    update(DataState.Failed(e))
                }
            }
        }

        activeJob.set(job)

        return job
    }
}

fun <T> dataFlowOf(initialState: DataState<T> = DataState.Uninitialized) =
    DataFlow<T>(initialState)

@Suppress("unused")
fun anyLoading(vararg dataState: DataState<*>): Boolean {
    return dataState.any { it is DataState.Loading }
}

@Suppress("unused")
fun anyLoadingOrUninitialized(vararg dataState: DataState<*>): Boolean {
    return dataState.any { it is DataState.Loading || it is DataState.Uninitialized }
}

@Suppress("unused")
fun allSuccess(vararg dataState: DataState<*>): Boolean {
    return dataState.all { it is DataState.Success }
}

@Suppress("unused")
fun allFailed(vararg dataState: DataState<*>): Boolean {
    return dataState.all { it is DataState.Failed }
}

@Suppress("unused")
fun anyFailed(vararg dataState: DataState<*>): Boolean {
    return dataState.any { it is DataState.Failed }
}