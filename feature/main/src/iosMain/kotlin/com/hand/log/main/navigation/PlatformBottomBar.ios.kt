package com.hand.log.main.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.LocalDarkTheme
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.platform.NavigationBarEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.collections.immutable.PersistentList
import org.jetbrains.compose.resources.stringResource
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UITabBarItemAppearance
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformBottomBar(
	visible: Boolean,
	bottomItems: PersistentList<MainBottomNavItem>,
	currentItem: MainBottomNavItem?,
	onItemClick: (MainTabRoute) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val isDark = LocalDarkTheme.current
	val labels = bottomItems.map { stringResource(it.labelRes) }
	val currentBottomItems by rememberUpdatedState(bottomItems)
	val currentOnItemClick by rememberUpdatedState(onItemClick)
	val delegate = remember {
		TabBarDelegate { index ->
			currentBottomItems.getOrNull(index)?.let { currentOnItemClick(it.route) }
		}
	}
	val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
	var measuredHeight by remember { mutableStateOf<Dp?>(null) }

	NavigationBarEffect(backgroundColor = if (visible) colors.background else colors.card)

	AnimatedVisibility(
		visible = visible,
		enter = fadeIn() + slideIn { IntOffset(0, it.height) },
		exit = fadeOut() + slideOut { IntOffset(0, it.height) },
	) {
		UIKitView(
			factory = {
				TabBarContainer(
					onHeightChange = { measuredHeight = it.dp },
				).apply { tabBar.delegate = delegate }
			},
			modifier = Modifier
				.fillMaxWidth()
				.height((measuredHeight ?: DEFAULT_TAB_BAR_HEIGHT) + bottomInset),
			update = { container ->
				container.backgroundColor = colors.background.toUIColor()
				val tabBar = container.tabBar
				tabBar.overrideUserInterfaceStyle = if (isDark) {
					UIUserInterfaceStyle.UIUserInterfaceStyleDark
				} else {
					UIUserInterfaceStyle.UIUserInterfaceStyleLight
				}
				tabBar.applyAppearance(
					background = colors.card,
					selected = colors.primary,
					unselected = colors.textSecondary,
				)
				val currentTitles = tabBar.items?.map { (it as UITabBarItem).title }
				if (currentTitles != labels) {
					val items = bottomItems.mapIndexed { index, item ->
						UITabBarItem(
							title = labels[index],
							image = UIImage.systemImageNamed(item.sfSymbol),
							selectedImage = UIImage.systemImageNamed(item.sfSymbolSelected),
						).apply { tag = index.toLong() }
					}
					tabBar.setItems(items, animated = false)
				}
				tabBar.selectedItem = tabBar.items?.getOrNull(bottomItems.indexOf(currentItem)) as? UITabBarItem
			},
			properties = UIKitInteropProperties(
				isInteractive = true,
				isNativeAccessibilityEnabled = true,
			),
		)
	}
}

private val DEFAULT_TAB_BAR_HEIGHT = 49.dp

@OptIn(ExperimentalForeignApi::class)
private class TabBarContainer(
	private val onHeightChange: (Double) -> Unit,
) : UIView(frame = CGRectZero.readValue()) {
	val tabBar = UITabBar()
	private var lastHeight = 0.0

	init {
		addSubview(tabBar)
	}

	override fun layoutSubviews() {
		super.layoutSubviews()
		val width = bounds.useContents { size.width }
		val fitHeight = tabBar.sizeThatFits(CGSizeMake(width, 0.0)).useContents { height }
		tabBar.setFrame(CGRectMake(0.0, 0.0, width, fitHeight))
		if (window != null && fitHeight != lastHeight) {
			lastHeight = fitHeight
			onHeightChange(fitHeight)
		}
	}
}

private class TabBarDelegate(
	private val onSelect: (Int) -> Unit,
) : NSObject(), UITabBarDelegateProtocol {
	override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
		onSelect(didSelectItem.tag.toInt())
	}
}

private fun UITabBar.applyAppearance(background: Color, selected: Color, unselected: Color) {
	val itemAppearance = UITabBarItemAppearance().apply {
		normal.iconColor = unselected.toUIColor()
		normal.titleTextAttributes = mapOf<Any?, Any?>(NSForegroundColorAttributeName to unselected.toUIColor())
		this.selected.iconColor = selected.toUIColor()
		this.selected.titleTextAttributes = mapOf<Any?, Any?>(NSForegroundColorAttributeName to selected.toUIColor())
	}
	val appearance = UITabBarAppearance().apply {
		configureWithOpaqueBackground()
		backgroundColor = background.toUIColor()
		stackedLayoutAppearance = itemAppearance
		inlineLayoutAppearance = itemAppearance
		compactInlineLayoutAppearance = itemAppearance
	}
	standardAppearance = appearance
	scrollEdgeAppearance = appearance
}

private fun Color.toUIColor(): UIColor = UIColor.colorWithRed(
	red = red.toDouble(),
	green = green.toDouble(),
	blue = blue.toDouble(),
	alpha = alpha.toDouble(),
)

private val MainBottomNavItem.sfSymbol: String
	get() = when (this) {
		MainBottomNavItem.Home -> "house"
		MainBottomNavItem.Players -> "person.2"
		MainBottomNavItem.Chart -> "square.grid.3x3"
		MainBottomNavItem.Settings -> "gearshape"
	}

private val MainBottomNavItem.sfSymbolSelected: String
	get() = when (this) {
		MainBottomNavItem.Home -> "house.fill"
		MainBottomNavItem.Players -> "person.2.fill"
		MainBottomNavItem.Chart -> "square.grid.3x3.fill"
		MainBottomNavItem.Settings -> "gearshape.fill"
	}
