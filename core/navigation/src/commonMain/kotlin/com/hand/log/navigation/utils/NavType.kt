package com.hand.log.navigation.utils

/**
 * This file previously contained custom NavType implementations for Navigation 2.x.
 *
 * In Navigation 3.0, custom NavTypes are no longer needed.
 * Type-safe navigation with Kotlin Serialization automatically handles
 * serialization and deserialization of navigation arguments.
 *
 * If you need to pass complex objects between destinations:
 * 1. Make sure your data classes are annotated with @Serializable
 * 2. Use them directly in your route definitions
 * 3. Navigation Compose will handle the rest automatically
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class UserRoute(val userId: String, val userName: String)
 *
 * // Define route
 * composable<UserRoute> { backStackEntry ->
 *     val route = backStackEntry.toRoute<UserRoute>()
 *     UserScreen(userId = route.userId, userName = route.userName)
 * }
 *
 * // Navigate
 * navController.navigate(UserRoute(userId = "123", userName = "John"))
 * ```
 */

