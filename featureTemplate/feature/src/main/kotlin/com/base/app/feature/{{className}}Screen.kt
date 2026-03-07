package {{packageName}}.{{featureName}}

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import {{packageName}}.designsystem.theme.HmmMaterialTheme
import {{packageName}}.designsystem.etc.ThemePreviews

@Composable
internal fun {{className}}Screen(
    {{lowerCamelClassName}}State: {{className}}State.{{className}}Data,
) {
    {{className}}Screen(
    )
}

@ThemePreview
@Composable
private fun {{className}}ScreenPreview() {
	HmmMaterialTheme {
        {{className}}Screen(

        )
    }
}
