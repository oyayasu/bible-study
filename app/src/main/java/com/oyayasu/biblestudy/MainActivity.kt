package com.oyayasu.biblestudy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.oyayasu.biblestudy.data.JournalEntry
import com.oyayasu.biblestudy.data.LocalStudyStore
import com.oyayasu.biblestudy.data.Study
import com.oyayasu.biblestudy.data.StudyCatalog
import com.oyayasu.biblestudy.reminders.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Wine = Color(0xFF784756)
private val WineDark = Color(0xFF54313D)
private val Gold = Color(0xFFB58C52)
private val Ink = Color(0xFF2C2928)
private val Muted = Color(0xFF847B77)
private val Cream = Color(0xFFF8F6F2)
private val Line = Color(0xFFEDE5DF)
private val DayFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ReminderScheduler.scheduleAll(this)
        setContent { BibleStudyApplication() }
    }

    override fun onResume() {
        super.onResume()
        ReminderScheduler.scheduleAll(this)
    }
}

@Composable
private fun BibleStudyApplication() {
    val context = LocalContext.current
    val store = remember { LocalStudyStore(context.applicationContext) }
    var route by rememberSaveable { mutableStateOf("home") }
    var activeStudyId by rememberSaveable { mutableStateOf(StudyCatalog.studies.first().id) }
    var activeSession by rememberSaveable { mutableStateOf("morning") }

    val today = LocalDate.now()
    val studyOfDay = StudyCatalog.studies[
        Math.floorMod(today.toEpochDay(), StudyCatalog.studies.size.toLong()).toInt()
    ]
    val onOpenStudy: (Study, String) -> Unit = { study, session ->
        activeStudyId = study.id
        activeSession = session
        route = "reader"
    }
    val scheme = lightColorScheme(
        primary = Wine, onPrimary = Color.White,
        background = Cream, onBackground = Ink,
        surface = Color.White, onSurface = Ink,
        secondary = Gold
    )
    MaterialTheme(colorScheme = scheme) {
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFFCFAF7), Cream, Color(0xFFF6F0EC)))
        )) {
            // Soft Gaussian-blurred lights behind the translucent material.
            Box(
                Modifier.align(Alignment.TopEnd).offset(x = 100.dp, y = 70.dp)
                    .size(240.dp).blur(75.dp).background(Color(0x25B893A1), CircleShape)
            )
            Box(
                Modifier.align(Alignment.BottomStart).offset(x = (-100).dp, y = 20.dp)
                    .size(250.dp).blur(85.dp).background(Color(0x1CB99668), CircleShape)
            )
            Column(
                Modifier.fillMaxSize().safeDrawingPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.fillMaxWidth().widthIn(max = 760.dp)) {
                        when (route) {
                            "home" -> HomeScreen(today, studyOfDay, store, onOpenStudy)
                            "library" -> LibraryScreen(onOpenStudy)
                            "journal" -> JournalScreen(store, onOpenStudy)
                            "settings" -> SettingsScreen(store)
                            "reader" -> ReaderScreen(
                                StudyCatalog.find(activeStudyId),
                                activeSession,
                                today,
                                store,
                                onBack = { route = if (activeSession == "library") "library" else "home" }
                            )
                        }
                    }
                }
                if (route != "reader") BottomNavigationBar(route) { route = it }
            }
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier.clip(RoundedCornerShape(25.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, Color.White.copy(alpha = 0.95f), RoundedCornerShape(25.dp))
            .padding(padding),
        content = content
    )
}

@Composable
private fun AppHeader(detail: String? = null) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(13.dp))
                    .background(Wine),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.AutoStories, null, tint = Color(0xFFF7E6C8), modifier = Modifier.size(22.dp))
            }
            Column {
                Text("BÍBLIA", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 2.sp, color = WineDark)
                Text("ESTUDO & MEDITAÇÃO", color = Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
            }
        }
        if (detail != null) {
            Text(detail, fontSize = 11.sp, color = Muted)
        } else {
            Icon(Icons.Outlined.Spa, null, tint = Gold, modifier = Modifier.size(23.dp))
        }
    }
}

@Composable
private fun PageTitle(kicker: String, title: String, description: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(kicker.uppercase(Locale("pt", "BR")), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = Gold)
        Text(title, fontSize = 32.sp, lineHeight = 37.sp, color = Ink, fontFamily = FontFamily.Serif)
        if (description != null) Text(description, fontSize = 13.sp, lineHeight = 20.sp, color = Muted)
    }
}

@Composable
private fun AccentButton(
    label: String,
    icon: ImageVector = Icons.Outlined.ArrowForward,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Wine, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 17.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(9.dp))
        Icon(icon, null, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun Tag(text: String, dark: Boolean = false) {
    Box(
        Modifier.clip(CircleShape)
            .background(if (dark) Color.White.copy(alpha = 0.17f) else Color(0xFFF4EAE8))
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(text, color = if (dark) Color.White else Wine, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HomeScreen(
    today: LocalDate,
    todayStudy: Study,
    store: LocalStudyStore,
    openStudy: (Study, String) -> Unit
) {
    val refreshed = store.revision // Re-render when the local completion state changes.
    val morningDone = store.isComplete(today, "morning")
    val eveningDone = store.isComplete(today, "evening")
    val upcoming = if (LocalTime.now().hour < 13) "morning" else "evening"

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 23.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { AppHeader() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(today.format(DayFormatter).replaceFirstChar { it.uppercase() }, color = Muted, fontSize = 12.sp)
                Text("Um encontro diário\ncom a Palavra.", color = Ink, fontFamily = FontFamily.Serif, fontSize = 35.sp, lineHeight = 40.sp)
            }
        }
        item {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(29.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(WineDark, Wine, Color(0xFFAD7885))
                        )
                    )
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Tag("ESTUDO DE HOJE", dark = true)
                        Icon(Icons.Outlined.AutoAwesome, null, tint = Color(0xFFE7C7A3), modifier = Modifier.size(20.dp))
                    }
                    Text(todayStudy.title, fontFamily = FontFamily.Serif, fontSize = 29.sp,
                        lineHeight = 33.sp, color = Color.White)
                    Text(todayStudy.reference + "  •  " + todayStudy.theme, color = Color(0xFFF0DEE1), fontSize = 12.sp)
                    Text(todayStudy.subtitle, color = Color(0xFFF6EBEB), fontSize = 13.sp, lineHeight = 19.sp)
                    Button(
                        onClick = { openStudy(todayStudy, upcoming) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = WineDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Começar meditação", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(9.dp))
                        Icon(Icons.Outlined.ArrowForward, null, Modifier.size(16.dp))
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Seus momentos", fontSize = 20.sp, fontFamily = FontFamily.Serif, color = Ink)
                MeditationRow("Manhã", "06h00", "Começar com intenção", Icons.Outlined.WbSunny,
                    morningDone) { openStudy(todayStudy, "morning") }
                MeditationRow("Noite", "20h30", "Revisitar o dia", Icons.Outlined.NightsStay,
                    eveningDone) { openStudy(todayStudy, "evening") }
            }
        }
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.Lightbulb, null, tint = Gold, modifier = Modifier.size(22.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Um passo de cada vez", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
                        Text("A constância importa mais que uma sequência perfeita de dias.",
                            fontSize = 12.sp, lineHeight = 18.sp, color = Muted)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun MeditationRow(
    title: String,
    time: String,
    description: String,
    icon: ImageVector,
    completed: Boolean,
    onClick: () -> Unit
) {
    GlassCard(Modifier.fillMaxWidth().clickable(onClick = onClick), padding = 15.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(50.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFFF7EFEB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Wine, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title + "  ·  " + time, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(if (completed) "Concluída hoje" else description, color = Muted, fontSize = 12.sp)
            }
            Icon(
                if (completed) Icons.Outlined.CheckCircle else Icons.Outlined.ChevronRight,
                null, tint = if (completed) Color(0xFF719782) else Muted,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun LibraryScreen(openStudy: (Study, String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTag by rememberSaveable { mutableStateOf("Todos") }
    val tags = listOf("Todos", "Misericórdia", "Arrependimento", "Vida prática")
    val studies = StudyCatalog.studies.filter {
        (selectedTag == "Todos" || selectedTag == it.theme) &&
            (query.isBlank() || listOf(it.title, it.reference, it.subtitle, it.theme)
                .any { value -> value.contains(query, ignoreCase = true) })
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 23.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { AppHeader() }
        item { PageTitle("Biblioteca", "Conheça a Palavra.", "Estudos introdutórios. Seu repertório definitivo será incluído depois.") }
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Buscar passagem ou tema", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Muted) },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Wine, unfocusedBorderColor = Line,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                )
            )
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag, fontSize = 11.sp) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Wine, selectedLabelColor = Color.White,
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, if (selectedTag == tag) Wine else Line)
                    )
                }
            }
        }
        if (studies.isEmpty()) {
            item { Text("Nenhuma passagem encontrada.", color = Muted, fontSize = 13.sp) }
        }
        items(studies, key = { it.id }) { study ->
            GlassCard(Modifier.fillMaxWidth().clickable { openStudy(study, "library") }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tag(study.theme)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Outlined.ArrowOutward, null, tint = Muted, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(study.title, fontSize = 22.sp, fontFamily = FontFamily.Serif, color = Ink)
                Spacer(Modifier.height(4.dp))
                Text(study.reference, fontSize = 12.sp, color = Wine, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(9.dp))
                Text(study.subtitle, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ReaderScreen(
    study: Study,
    session: String,
    today: LocalDate,
    store: LocalStudyStore,
    onBack: () -> Unit
) {
    var segment by rememberSaveable(study.id) { mutableIntStateOf(0) }
    var portugueseText by remember(study.id) { mutableStateOf(store.passage(study.id)) }
    var reflection by remember(today, study.id, session) {
        mutableStateOf(store.note(today, study.id, session))
    }
    var savedPassage by remember { mutableStateOf(false) }
    var savedReflection by remember { mutableStateOf(false) }
    val isMeditation = session != "library"
    val prompt = if (session == "evening") study.eveningPrompt else study.morningPrompt

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar", tint = Ink)
                }
                Spacer(Modifier.width(7.dp))
                Text(if (session == "morning") "MEDITAÇÃO DA MANHÃ"
                    else if (session == "evening") "MEDITAÇÃO DA NOITE" else "BIBLIOTECA",
                    color = Wine, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            PageTitle(study.theme, study.title, study.reference + " · " + study.subtitle)
        }
        item {
            GlassCard {
                Text("ENTENDA A PASSAGEM", fontSize = 10.sp, letterSpacing = 1.6.sp, color = Gold, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Text(study.overview, color = Ink, fontSize = 14.sp, lineHeight = 23.sp)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("Português", "Original", "Contexto").forEachIndexed { index, name ->
                    val selected = segment == index
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(13.dp))
                            .background(if (selected) Wine else Color.White.copy(alpha = 0.85f))
                            .clickable { segment = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name, color = if (selected) Color.White else Muted, fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
        item {
            when (segment) {
                0 -> GlassCard {
                    Text("BÍBLIA DE JERUSALÉM", fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp, letterSpacing = 1.5.sp, color = Gold)
                    Spacer(Modifier.height(10.dp))
                    Text("Leitura: " + study.reference, fontFamily = FontFamily.Serif, fontSize = 21.sp, color = Ink)
                    Spacer(Modifier.height(10.dp))
                    if (portugueseText.isBlank()) {
                        Text("O texto dessa tradução não acompanha o app por ser protegido por direitos autorais. Consulte seu exemplar físico ou insira um trecho de uso pessoal abaixo.",
                            color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
                    } else {
                        Text("Texto pessoal salvo neste aparelho", fontSize = 11.sp, color = Muted)
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = portugueseText,
                        onValueChange = { portugueseText = it; savedPassage = false },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 165.dp),
                        placeholder = { Text("Seu texto em português...", color = Muted, fontSize = 13.sp) },
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Wine, unfocusedBorderColor = Line
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = {
                        store.savePassage(study.id, portugueseText)
                        savedPassage = true
                    }) {
                        Icon(Icons.Outlined.Save, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(if (savedPassage) "Texto salvo" else "Salvar texto neste aparelho", fontSize = 12.sp)
                    }
                }
                1 -> GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Translate, null, tint = Gold)
                        Spacer(Modifier.width(8.dp))
                        Text(study.originalLanguage.uppercase(Locale("pt", "BR")),
                            fontSize = 10.sp, letterSpacing = 1.5.sp, color = Gold,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(study.originalWord, fontSize = 35.sp, lineHeight = 46.sp, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    Text(study.transliteration, color = Wine, fontSize = 14.sp)
                    Spacer(Modifier.height(14.dp))
                    Text(study.originalMeaning, color = Ink, fontSize = 13.sp, lineHeight = 21.sp)
                    Spacer(Modifier.height(14.dp))
                    Text("Apenas uma palavra ou expressão demonstrativa, não o texto original completo. A integração de edições críticas e suas fontes está prevista para outra versão.",
                        color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
                }
                else -> GlassCard {
                    Text("CONTEXTO HISTÓRICO", fontSize = 10.sp, letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold, color = Gold)
                    Spacer(Modifier.height(14.dp))
                    Text(study.historicalContext, fontSize = 14.sp, lineHeight = 23.sp, color = Ink)
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = Line)
                    Spacer(Modifier.height(15.dp))
                    Text("LEITURA E INTERPRETAÇÃO", fontSize = 10.sp, letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold, color = Gold)
                    Spacer(Modifier.height(9.dp))
                    Text(study.overview, fontSize = 13.sp, lineHeight = 21.sp, color = Ink)
                    Spacer(Modifier.height(14.dp))
                    Text("Contextualização editorial introdutória, ainda não revisada contra uma bibliografia acadêmica selecionada.",
                        fontSize = 11.sp, lineHeight = 17.sp, color = Muted)
                }
            }
        }
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.EditNote, null, tint = Gold, modifier = Modifier.size(25.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Sua reflexão", fontFamily = FontFamily.Serif, fontSize = 23.sp, color = Ink)
                }
                Spacer(Modifier.height(12.dp))
                Text(if (isMeditation) prompt else study.morningPrompt,
                    color = WineDark, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
                Spacer(Modifier.height(10.dp))
                Text("Um passo prático", fontSize = 11.sp, color = Gold, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(study.practicalStep, fontSize = 13.sp, color = Ink, lineHeight = 20.sp)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it; savedReflection = false },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 135.dp),
                    placeholder = { Text("Escreva sua reflexão pessoal...", fontSize = 13.sp) },
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Wine, unfocusedBorderColor = Line
                    )
                )
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = {
                    store.saveNote(today, study.id, session, reflection)
                    savedReflection = true
                }) {
                    Icon(Icons.Outlined.Save, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(if (savedReflection) "Reflexão salva" else "Salvar no diário", fontSize = 12.sp)
                }
            }
        }
        if (isMeditation) {
            item {
                AccentButton(
                    label = if (store.isComplete(today, session)) "Meditação concluída" else "Concluir meditação",
                    icon = Icons.Outlined.CheckCircle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    store.saveNote(today, study.id, session, reflection)
                    store.setComplete(today, session, true)
                    onBack()
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun JournalScreen(
    store: LocalStudyStore,
    openStudy: (Study, String) -> Unit
) {
    val ignored = store.revision
    val entries = store.entries()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 23.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AppHeader() }
        item { PageTitle("Diário pessoal", "O que está mudando?", "Um registro privado das suas reflexões. Nada sai deste aparelho.") }
        if (entries.isEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth(), padding = 26.dp) {
                    Icon(Icons.Outlined.EditNote, null, tint = Gold, modifier = Modifier.size(33.dp))
                    Spacer(Modifier.height(14.dp))
                    Text("Seu diário começa com uma reflexão.", fontFamily = FontFamily.Serif,
                        fontSize = 22.sp, lineHeight = 27.sp, color = Ink)
                    Spacer(Modifier.height(9.dp))
                    Text("Abra uma meditação, escreva o que aprendeu e salve. Os registros aparecerão aqui.",
                        color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
        }
        items(entries) { entry ->
            val study = StudyCatalog.find(entry.studyId)
            GlassCard(Modifier.fillMaxWidth().clickable { openStudy(study, entry.session) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(entry.date, color = Gold, fontSize = 11.sp)
                    Text(
                        when (entry.session) {
                            "morning" -> "Manhã"
                            "evening" -> "Noite"
                            else -> "Estudo"
                        }, color = Muted, fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(study.title, fontFamily = FontFamily.Serif, fontSize = 21.sp, color = Ink)
                Spacer(Modifier.height(9.dp))
                Text(entry.text, maxLines = 4, overflow = TextOverflow.Ellipsis,
                    color = Ink, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun SettingsScreen(store: LocalStudyStore) {
    val context = LocalContext.current
    val ignored = store.revision
    var notificationAllowed by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    var exactAllowed by remember { mutableStateOf(ReminderScheduler.hasExactPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationAllowed = granted
        ReminderScheduler.scheduleAll(context)
    }
    val exactLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        exactAllowed = ReminderScheduler.hasExactPermission(context)
        ReminderScheduler.scheduleAll(context)
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 23.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        item { AppHeader() }
        item { PageTitle("Preferências", "Seu tempo com Deus.", "Configurações de uso pessoal. Sem conta, publicidade ou sincronização externa.") }
        item {
            GlassCard {
                Text("LEMBRETES", color = Gold, letterSpacing = 1.5.sp, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(16.dp))
                ReminderSwitchRow(
                    "Meditação da manhã", "Todos os dias às 06h00",
                    store.morningEnabled()
                ) {
                    store.setMorningEnabled(it)
                    ReminderScheduler.scheduleAll(context)
                }
                HorizontalDivider(Modifier.padding(vertical = 13.dp), color = Line)
                ReminderSwitchRow(
                    "Meditação da noite", "Todos os dias às 20h30",
                    store.eveningEnabled()
                ) {
                    store.setEveningEnabled(it)
                    ReminderScheduler.scheduleAll(context)
                }
                Spacer(Modifier.height(13.dp))
                Text("O fuso horário é o do aparelho. Os lembretes são reprogramados após reiniciar ou mudar o horário.",
                    color = Muted, fontSize = 11.sp, lineHeight = 18.sp)
            }
        }
        item {
            GlassCard {
                Text("PERMISSÕES", color = Gold, letterSpacing = 1.5.sp,
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(13.dp))
                StatusRow(
                    "Notificações",
                    if (notificationAllowed) "Permitidas" else "Precisam de autorização",
                    if (notificationAllowed) Icons.Outlined.CheckCircle else Icons.Outlined.NotificationsOff
                )
                if (!notificationAllowed) {
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = {
                        if (Build.VERSION.SDK_INT >= 33)
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }) { Text("Permitir notificações", fontSize = 12.sp) }
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Line)
                StatusRow(
                    "Precisão dos horários",
                    if (exactAllowed) "Alarmes exatos permitidos" else "Horários aproximados",
                    Icons.Outlined.Alarm
                )
                if (!exactAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = {
                        val intent = Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + context.packageName)
                        )
                        runCatching { exactLauncher.launch(intent) }
                    }) { Text("Permitir alarmes exatos", fontSize = 12.sp) }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Sem permissão para alarmes exatos, o Android pode atrasar os avisos, especialmente em economia de bateria.",
                    color = Muted, fontSize = 11.sp, lineHeight = 18.sp
                )
            }
        }
        item {
            GlassCard {
                Text("TRADUÇÃO", color = Gold, letterSpacing = 1.5.sp,
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Text("Bíblia de Jerusalém", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(Modifier.height(5.dp))
                Text("Paulus · Nova edição, revista e ampliada (2002)", fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(10.dp))
                Text("O texto protegido não é distribuído com o aplicativo. Você pode salvar seus próprios trechos localmente em cada estudo.",
                    fontSize = 12.sp, lineHeight = 19.sp, color = Ink)
            }
        }
        item {
            Text("VERSÃO 0.1 • DADOS SALVOS APENAS NO APARELHO", modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ReminderSwitchRow(title: String, detail: String, enabled: Boolean, changed: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = Muted, fontSize = 12.sp)
        }
        Switch(
            checked = enabled, onCheckedChange = changed,
            colors = SwitchDefaults.colors(checkedTrackColor = Wine)
        )
    }
}

@Composable
private fun StatusRow(title: String, detail: String, icon: ImageVector) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Wine, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(detail, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun BottomNavigationBar(active: String, select: (String) -> Unit) {
    val pages = listOf(
        Triple("home", "Hoje", Icons.Outlined.Home),
        Triple("library", "Estudos", Icons.Outlined.MenuBook),
        Triple("journal", "Diário", Icons.Outlined.EditNote),
        Triple("settings", "Ajustes", Icons.Outlined.Tune)
    )
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .background(Color.White.copy(alpha = 0.91f))
            .border(1.dp, Color.White.copy(alpha = 0.93f), RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        pages.forEach { (route, label, icon) ->
            val selected = active == route
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(15.dp))
                    .clickable { select(route) }.padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(icon, contentDescription = label, tint = if (selected) Wine else Muted,
                    modifier = Modifier.size(22.dp))
                Text(label, fontSize = 10.sp, color = if (selected) Wine else Muted,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}
