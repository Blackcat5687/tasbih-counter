package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class BadgeItem(
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val currentStreak = prefs.streak
    val dailyTarget = prefs.dailyTarget
    
    // Get total dhikr count
    val totalCount = remember(prefs) {
        DHIKR_LIST.sumOf { prefs.getDhikrCount(it.arabic) }
    }
    
    // Calculate last 7 days history
    val historyMap = remember(prefs) { prefs.getHistory() }
    val last7DaysData = remember(historyMap) {
        val list = mutableListOf<Pair<String, Int>>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displaySdf = SimpleDateFormat("E", Locale("ar")) // Arabic day name
        
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -6)
        
        for (i in 0..6) {
            val dateKey = sdf.format(cal.time)
            val dayName = displaySdf.format(cal.time)
            val count = historyMap[dateKey] ?: 0
            list.add(Pair(dayName, count))
            cal.add(Calendar.DATE, 1)
        }
        list
    }
    
    val maxCountInHistory = last7DaysData.maxOf { it.second }.coerceAtLeast(1)
    
    // Evaluate Achievements / Badges
    val badges = remember(totalCount, currentStreak) {
        listOf(
            BadgeItem(
                title = "بداية النور",
                description = "قمت بأول تسبيحة لك في التطبيق",
                isUnlocked = totalCount > 0,
                icon = Icons.Default.LockOpen
            ),
            BadgeItem(
                title = "المسبّح المواظب",
                description = "حافَظت على سلسلة يومية متواصلة من الأذكار",
                isUnlocked = currentStreak >= 2,
                icon = Icons.Default.LocalFireDepartment
            ),
            BadgeItem(
                title = "مئة طمأنينة",
                description = "تجاوزت حاجز المائة ذكر إجماليًا",
                isUnlocked = totalCount >= 100,
                icon = Icons.Default.Assessment
            ),
            BadgeItem(
                title = "الألفية المباركة",
                description = "أتممت ألف تسبيحة وذكر في صحيفتك",
                isUnlocked = totalCount >= 1000,
                icon = Icons.Default.EmojiEvents
            )
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "مؤشرات التقدم",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Stats Overview Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Count Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Total Counts",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("إجمالي الأذكار", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$totalCount",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("total_dhikr_count_text")
                                )
                            }
                        }

                        // Daily Streak Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("سلسلة أيامك", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$currentStreak يومًا",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("streak_count_text")
                                )
                            }
                        }
                    }
                }

                // 2. Custom Canvas Vertical Bar Chart (Last 7 Days)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "نشاط الأيام الـ ٧ الأخيرة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Bar chart layout
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                last7DaysData.forEach { data ->
                                    val dayName = data.first
                                    val countValue = data.second
                                    
                                    // Animate the height scale
                                    val barHeightRatio = countValue.toFloat() / maxCountInHistory.toFloat()
                                    val animatedRatio by animateFloatAsState(
                                        targetValue = barHeightRatio,
                                        animationSpec = tween(1000),
                                        label = "bar_height"
                                    )
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Number popup on top of active bars
                                        if (countValue > 0) {
                                            Text(
                                                text = "$countValue",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                        
                                        // Colored Bar cylinder
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight(0.75f * animatedRatio.coerceAtLeast(0.06f))
                                                .width(16.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = if (countValue > 0) {
                                                            listOf(
                                                                MaterialTheme.colorScheme.primary,
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                            )
                                                        } else {
                                                            listOf(
                                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                                            )
                                                        }
                                                    )
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        // Day Label
                                        Text(
                                            text = dayName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Achievements Badges section heading
                item {
                    Text(
                        "الأوسمة والإنجازات اليومية",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                    )
                }

                // 4. Milestone Badge list
                items(badges) { badge ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (badge.isUnlocked) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                            }
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (badge.isUnlocked) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge Icon container
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (badge.isUnlocked) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = badge.icon,
                                    contentDescription = badge.title,
                                    tint = if (badge.isUnlocked) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Badge details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = badge.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (badge.isUnlocked) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (badge.isUnlocked) {
                                        Text(
                                            "تم الفتح",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = badge.description,
                                    fontSize = 12.sp,
                                    color = if (badge.isUnlocked) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
