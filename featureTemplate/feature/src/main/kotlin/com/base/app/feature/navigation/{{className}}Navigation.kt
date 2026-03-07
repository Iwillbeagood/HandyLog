package {{packageName}}.{{featureName}}.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import {{packageName}}.{{featureName}}.{{className}}Route
import {{packageName}}.model.error.MessageType
import {{packageName}}.navigation.Route

// TODO: Navigation 모듈에 경로를 정의해야 합니다.
fun NavController.navigateTo{{className}}() {
    navigate(Route.{{className}})
}

fun NavGraphBuilder.{{lowerCamelClassName}}NavGraph(
    onGoBack: () -> Unit,
    onShowSnackBar: (MessageType) -> Unit
) {
    composable<Route.{{className}}> {
        {{className}}Route(
            onGoBack = onGoBack,
            onShowSnackBar = onShowSnackBar
        )
    }
}