package com.hand.log.navigation.utils

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute

/**
 * Type-safe SavedStateHandle extension for Navigation 3.0.
 *
 * This is a convenience wrapper around the toRoute() function
 * that maintains API compatibility while removing the deprecated typeMap parameter.
 *
 * In Navigation 3.0, Kotlin Serialization handles type serialization automatically.
 *
 * Example usage:
 * ```kotlin
 * class MyViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
 *     private val args: MyRoute = savedStateHandle.toRouteType<MyRoute>()
 * }
 * ```
 */
inline fun <reified T : Any> SavedStateHandle.toRouteType(): T = toRoute<T>()

