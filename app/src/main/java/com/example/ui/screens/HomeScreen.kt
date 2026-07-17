package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import kotlinx.coroutines.launch

val DHIKR_LIST = listOf(
    DhikrItem("سُبْحَانَ اللَّهِ", "Subhan Allah", "Glory be to Allah"),
    DhikrItem("الْحَمْدُ لِلَّهِ", "Alhamdulillah", "Praise be to Allah"),
    DhikrItem("اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah is the Greatest"),
    DhikrItem("لَا إِلَهَ إِلَّا اللَّهُ", "La ilaha illallah", "There is no god but Allah"),
    DhikrItem("أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah", "I seek forgiveness from Allah"),
    DhikrItem("اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", "Allahumma Salli Ala Muhammad", "Blessings upon Prophet Muhammad")
)

data class DhikrItem(
    val arabic: String,
    val transliteration: String,
    val translation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var currentDhikrIdx by remember { mutableStateOf(prefs.currentDhikrIndex) }
    val currentDhikr = DHIKR_LIST.getOrElse(currentDhikrIdx) { DHIKR_LIST[0] }
    
    var count by remember { mutableStateOf(prefs.getDhikrCount(currentDhikr.arabic)) }
    val target = prefs.dailyTarget
    val streak = prefs.streak
    
    // Scale animation for tap effect
    val scale = remember { Animatable(1f) }
    
    fun triggerVibration(intensity: Int = 1) {
        if (!prefs.hapticEnabled) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (intensity == 2) {
                    VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(if (intensity == 2) 150 else 50)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "المسبحة الإلكترونية",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        // Streak Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streak يوم",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Horizontal List of Dhikr Categories
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "اختر الذكر:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(DHIKR_LIST) { index, item ->
                            val isSelected = index == currentDhikrIdx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .then(
                                        if (isSelected) Modifier
                                        else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    )
                                    .clickable {
                                        currentDhikrIdx = index
                                        prefs.currentDhikrIndex = index
                                        count = prefs.getDhikrCount(item.arabic)
                                        triggerVibration()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .testTag("dhikr_chip_${index}")
                            ) {
                                Text(
                                    item.arabic,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Large Central Counter Dial
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(290.dp)
                        .scale(scale.value)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                count++
                                prefs.saveDhikrCount(currentDhikr.arabic, count)
                                if (count % target == 0) {
                                    triggerVibration(intensity = 2)
                                } else {
                                    triggerVibration()
                                }
                                scale.animateTo(
                                    targetValue = 1.05f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                )
                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                )
                            }
                        }
                        .testTag("tasbih_tap_area")
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val surfaceColor = MaterialTheme.colorScheme.surface
                    
                    // Circular Progress Dial Canvas
                    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        val backgroundStrokeWidth = 4.dp.toPx()
                        
                        // Background track
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.12f),
                            radius = size.minDimension / 2,
                            style = Stroke(width = backgroundStrokeWidth)
                        )
                        
                        // Active track progress
                        val progress = if (target > 0) (count % target).toFloat() / target.toFloat() else 0f
                        val sweepAngle = progress * 360f
                        
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(primaryColor, secondaryColor, primaryColor)
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Inner Circle Content Container
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .shadow(24.dp, shape = CircleShape, ambientColor = primaryColor.copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        surfaceColor,
                                        surfaceColor.copy(alpha = 0.95f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            // Current Active Dhikr text (Large Arabic font)
                            Text(
                                text = currentDhikr.arabic,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Transliteration
                            Text(
                                text = currentDhikr.transliteration,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Counter value
                            Text(
                                text = "$count",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("current_count_text")
                            )

                            // Target Tracker indicator
                            Text(
                                text = "الهدف: $target",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    Button(
                        onClick = {
                            triggerVibration(intensity = 2)
                            count = 0
                            prefs.resetDhikrCount(currentDhikr.arabic)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp).testTag("reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصفير", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Auto Goal Increment (+33)
                    Button(
                        onClick = {
                            triggerVibration()
                            count += 33
                            prefs.saveDhikrCount(currentDhikr.arabic, count)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp).testTag("plus_33_button")
                    ) {
                        Text("+٣٣", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 4. Spiritual Quote / Dhikr Wisdom Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Wisdom Icon",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "«أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ»\nحافظ على وردك اليومي لتبني عادة إيمانية راسخة.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
