package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.theme.TasbihTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val prefs = PreferencesManager(applicationContext)
        prefs.verifyStreakOnLaunch()

        setContent {
            var selectedTheme by remember {
                mutableStateOf<ThemeType>(
                    try {
                        ThemeType.valueOf(prefs.selectedTheme.uppercase())
                    } catch (e: Exception) {
                        ThemeType.EMERALD
                    }
                )
            }

            TasbihTheme(themeType = selectedTheme) {
                MainAppLayout(
                    prefs = prefs,
                    currentTheme = selectedTheme,
                    onThemeChanged = { newTheme ->
                        selectedTheme = newTheme
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppLayout(
    prefs: PreferencesManager,
    currentTheme: ThemeType,
    onThemeChanged: (ThemeType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavigationItem(
            title = "الرئيسية",
            activeIcon = Icons.Filled.Home,
            inactiveIcon = Icons.Outlined.Home,
            testTag = "tab_home"
        ),
        NavigationItem(
            title = "المحادثات",
            activeIcon = Icons.Filled.ChatBubble,
            inactiveIcon = Icons.Outlined.ChatBubbleOutline,
            testTag = "tab_chats"
        ),
        NavigationItem(
            title = "التقدم",
            activeIcon = Icons.Filled.Assessment,
            inactiveIcon = Icons.Outlined.Assessment,
            testTag = "tab_progress"
        ),
        NavigationItem(
            title = "المزيد",
            activeIcon = Icons.Filled.Person,
            inactiveIcon = Icons.Outlined.Person,
            testTag = "tab_more"
        )
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val backgroundGradient = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
            )
        )

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        val activeIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                        val activeLabelColor = MaterialTheme.colorScheme.primary
                        val pillColor = MaterialTheme.colorScheme.secondaryContainer
                        val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            label = {
                                Text(
                                    item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) activeLabelColor else inactiveColor
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = activeIconColor,
                                unselectedIconColor = inactiveColor,
                                selectedTextColor = activeLabelColor,
                                unselectedTextColor = inactiveColor,
                                indicatorColor = pillColor
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(180))
                    },
                    label = "tab_content_fade"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeScreen(prefs = prefs)
                        1 -> ChatScreen()
                        2 -> ProgressScreen(prefs = prefs)
                        3 -> MoreScreen(prefs = prefs, onThemeChanged = onThemeChanged)
                        else -> HomeScreen(prefs = prefs)
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
