package tech.bashga.plata.screens.invest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_lesson_buy_stocks
import plata.shared.generated.resources.raster_lesson_complete_check
import plata.shared.generated.resources.raster_lesson_market_order_chart
import plata.shared.generated.resources.raster_lesson_plata_inversiones
import plata.shared.generated.resources.raster_lesson_stock_cubes
import tech.bashga.plata.designsystem.components.BulletItem
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.ButtonStyle
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.HeadlineBlockAlign
import tech.bashga.plata.designsystem.components.HeadlineBlockSize
import tech.bashga.plata.designsystem.components.IconButton
import tech.bashga.plata.designsystem.components.IconButtonSize
import tech.bashga.plata.designsystem.components.IconButtonStyle
import tech.bashga.plata.designsystem.components.LessonCard
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.NavigationBarStyle
import tech.bashga.plata.designsystem.components.ProgressBar
import tech.bashga.plata.designsystem.components.ProgressBarSize
import tech.bashga.plata.designsystem.components.TextButton
import tech.bashga.plata.designsystem.components.rememberCollapsed
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.backdropSource
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.PlataBackHandler
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Immutable
data class LessonContentPage(val media: DrawableResource, val title: String, val paragraphs: List<AnnotatedString>)

/** A lesson: the intro (title, read time, topics), its content pages and the lesson the Complete page offers next. */
@Immutable
data class Lesson(
    val id: String,
    val title: String,
    val readTime: String,
    val topics: List<String>,
    val cover: DrawableResource,
    val pages: List<LessonContentPage>,
    val nextId: String,
    val nextTitle: String,
    val nextReadTime: String,
    val nextThumbnail: DrawableResource,
)

object InvestLessonCatalog {
    val HowToBuyStocks = Lesson(
        id = InvestLessons.HOW_TO_BUY_STOCKS,
        title = "How to buy stocks?",
        readTime = "2-min read",
        topics = listOf("What stocks are", "Types of stock market orders"),
        cover = Res.drawable.raster_lesson_buy_stocks,
        pages = listOf(
            LessonContentPage(
                Res.drawable.raster_lesson_stock_cubes,
                "First, what is a stock?",
                listOf(
                    "Stocks are like tiny pieces of ownership in a company. When you own a stock, you own a bit of that company and its success can make your stock more valuable.",
                    "👉🏼 If you own a stock you can receive dividends (fraction of profits the company makes)",
                    "👉🏼 Or if the company does well, the price of the stock goes up and you can sell to earn the price difference.",
                ).map(::AnnotatedString),
            ),
            LessonContentPage(
                Res.drawable.raster_lesson_market_order_chart,
                "Types of stock prices:\nmarket order",
                listOf(
                    AnnotatedString(
                        "Since there are many participants on the market, the price is always changing. Imagine a regular Sunday market where a seller and a buyer try to bargain and find a match. On the stock market, it is similar: you see just the price but not the seller.",
                    ),
                    buildAnnotatedString {
                        append("A ")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("market order") }
                        append(" is an instruction to buy or sell a stock immediately at the current market price.\n")
                        append("➕ This type of order ensures quick execution.\n")
                        append("➖ Doesn't guarantee the price at which the trade will be completed, making it suitable for highly liquid stocks.")
                    },
                ),
            ),
        ),
        nextId = InvestLessons.PLATA_INVERSIONES,
        nextTitle = "What is Plata\nInversiones",
        nextReadTime = "2-minute read",
        nextThumbnail = Res.drawable.raster_lesson_plata_inversiones,
    )

    /** Only How to buy stocks has pages in the references; every other lesson card opens it too. */
    fun lesson(id: String): Lesson = listOf(HowToBuyStocks).firstOrNull { it.id == id } ?: HowToBuyStocks
}

sealed interface LessonPage {
    data object Intro : LessonPage
    data class Content(val index: Int) : LessonPage
    data object Complete : LessonPage
}

@Immutable
data class LessonState(val lesson: Lesson = InvestLessonCatalog.HowToBuyStocks, val page: LessonPage = LessonPage.Intro) {
    /** The nav bar fills to the middle of the current page's share: 25% and 75% for two pages (7ab581a2, 8de66942). */
    val progress: Float
        get() = when (val p = page) {
            is LessonPage.Content -> (p.index + 0.5f) / lesson.pages.size
            LessonPage.Complete -> 1f
            LessonPage.Intro -> 0f
        }
}

sealed interface LessonAction {
    data object GetStarted : LessonAction
    data object Next : LessonAction
    data object Back : LessonAction
    data object Close : LessonAction
    data object OpenNext : LessonAction
}

fun LessonState.reduce(action: LessonAction): LessonState = when (action) {
    LessonAction.GetStarted -> copy(page = LessonPage.Content(0))
    LessonAction.Next -> when (val p = page) {
        LessonPage.Intro -> copy(page = LessonPage.Content(0))
        is LessonPage.Content -> copy(page = if (p.index + 1 < lesson.pages.size) LessonPage.Content(p.index + 1) else LessonPage.Complete)
        LessonPage.Complete -> this
    }
    LessonAction.Back -> when (val p = page) {
        LessonPage.Intro -> this
        is LessonPage.Content -> copy(page = if (p.index == 0) LessonPage.Intro else LessonPage.Content(p.index - 1))
        LessonPage.Complete -> copy(page = LessonPage.Content(lesson.pages.lastIndex))
    }
    LessonAction.Close, LessonAction.OpenNext -> this
}

class LessonViewModel(lessonId: String) : ViewModel() {
    private val _state = MutableStateFlow(LessonState(InvestLessonCatalog.lesson(lessonId)))
    val state: StateFlow<LessonState> = _state.asStateFlow()

    fun onAction(action: LessonAction) {
        _state.value = _state.value.reduce(action)
    }
}

/** learn/{lessonId}: a full-screen lesson; back walks the pages, [onClose] dismisses the cover. */
@Composable
fun LessonScreen(lessonId: String, onClose: () -> Unit, onNextLesson: (String) -> Unit) {
    val viewModel = viewModel { LessonViewModel(lessonId) }
    val state by viewModel.state.collectAsState()
    PlataBackHandler(enabled = state.page != LessonPage.Intro) { viewModel.onAction(LessonAction.Back) }
    LessonContent(state, onAction = { action ->
        when (action) {
            LessonAction.Close -> onClose()
            LessonAction.OpenNext -> onNextLesson(state.lesson.nextId)
            else -> viewModel.onAction(action)
        }
    })
}

@Composable
fun LessonContent(state: LessonState, onAction: (LessonAction) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        when (val page = state.page) {
            LessonPage.Intro -> LessonIntro(state.lesson, onAction)
            is LessonPage.Content -> LessonContentPageView(state.lesson.pages[page.index], state.progress, onAction)
            LessonPage.Complete -> LessonComplete(state.lesson, onAction)
        }
    }
}

// c5882da1: the white photo band ends at y 433, the title's line box 28 below it, the bullets 14 apart
private val PhotoBandHeight = 433.dp
private val IntroTitleTop = 28.dp
private val TopicGap = 14.dp

@Composable
private fun LessonIntro(lesson: Lesson, onAction: (LessonAction) -> Unit) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().height(PhotoBandHeight).background(colors.surfaceCard)) {
            Image(
                painterResource(lesson.cover),
                contentDescription = null,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
            IconButton(
                PlataIcons.CloseX,
                { onAction(LessonAction.Close) },
                Modifier.align(Alignment.TopEnd).padding(top = safeArea.top + 1.dp, end = spacing.s16),
                style = IconButtonStyle.Grey,
                size = IconButtonSize.S32,
                onCard = true,
                glyphSize = 17.dp,
                contentDescription = "Close",
            )
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(start = spacing.screenMargin, end = spacing.screenMargin, bottom = safeArea.bottom + spacing.s20),
        ) {
            Spacer(Modifier.height(IntroTitleTop))
            HeadlineBlock(
                lesson.title,
                Modifier.fillMaxWidth(),
                body = lesson.readTime,
                bodyStyle = PlataTheme.typography.body,
                bodyColor = colors.textTertiary,
                gap = spacing.s4,
            )
            Spacer(Modifier.height(spacing.s24))
            Column(verticalArrangement = Arrangement.spacedBy(TopicGap)) {
                lesson.topics.forEach { BulletItem(it) }
            }
            Spacer(Modifier.weight(1f))
            Button("Get started", { onAction(LessonAction.GetStarted) }, Modifier.fillMaxWidth())
        }
    }
}

// lesson pages: the 220-high media card 11 under the bar, the title 34 below it; the nav progress bar is 140 wide
private val MediaHeight = 220.dp
private val MediaTop = 11.dp
private val PageTitleTop = 34.dp
private val ProgressWidth = 140.dp
private val ParagraphGap = 10.dp

// NavigationBar's status band stops 5 short of the safe area
private val StatusBarOverlap = 5.dp

@Composable
private fun LessonContentPageView(page: LessonContentPage, progress: Float, onAction: (LessonAction) -> Unit) {
    val spacing = PlataTheme.spacing
    val safeArea = PlataSafeArea.current
    // a scroll state per page, so each page opens at its top
    val scroll = remember(page) { ScrollState(0) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // at least a screen tall, so a short page still puts the button at the bottom
        Column(
            Modifier
                .fillMaxSize()
                .backdropSource()
                .verticalScroll(scroll)
                .heightIn(min = maxHeight)
                .padding(
                    start = spacing.screenMargin,
                    top = safeArea.top - StatusBarOverlap + PlataTheme.sizes.nav,
                    end = spacing.screenMargin,
                    bottom = safeArea.bottom + spacing.s20,
                ),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Spacer(Modifier.height(MediaTop))
                Image(
                    painterResource(page.media),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(MediaHeight).clip(ContinuousRoundedShape(PlataTheme.radii.xl)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(PageTitleTop))
                HeadlineBlock(page.title, Modifier.fillMaxWidth(), size = HeadlineBlockSize.Medium)
                Spacer(Modifier.height(spacing.s16))
                Column(verticalArrangement = Arrangement.spacedBy(ParagraphGap)) {
                    page.paragraphs.forEach {
                        PlataText(it, PlataTheme.typography.bodyRelaxed, PlataTheme.colors.textPrimary, Modifier.fillMaxWidth())
                    }
                }
                Spacer(Modifier.height(spacing.s20))
            }
            Button("Next", { onAction(LessonAction.Next) }, Modifier.fillMaxWidth())
        }
        NavigationBar(
            style = NavigationBarStyle.Progress,
            includeStatusBar = true,
            onBack = { onAction(LessonAction.Back) },
            center = { ProgressBar(progress, Modifier.width(ProgressWidth), size = ProgressBarSize.Nav) },
            trailing = { TextButton("Close", { onAction(LessonAction.Close) }) },
            collapsed = rememberCollapsed(scroll),
        )
    }
}

// 48490884: the check sits 145 under the bar
private val CheckTop = 150.dp

@Composable
private fun LessonComplete(lesson: Lesson, onAction: (LessonAction) -> Unit) {
    val spacing = PlataTheme.spacing
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(bottom = PlataSafeArea.current.bottom + spacing.s20)) {
            NavigationBar(trailing = { TextButton("Close", { onAction(LessonAction.Close) }) }, includeStatusBar = true)
            Spacer(Modifier.height(CheckTop))
            HeadlineBlock(
                "Lesson complete!",
                Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin),
                body = "Dive into the next one and keep\nsharpening your investing skills",
                align = HeadlineBlockAlign.Center,
                mediaGap = spacing.s16,
                media = { Image(painterResource(Res.drawable.raster_lesson_complete_check), null, Modifier.size(97.dp, 102.dp)) },
            )
            Spacer(Modifier.weight(1f))
            Column(Modifier.padding(horizontal = spacing.screenMargin)) {
                LessonCard(
                    lesson.nextTitle,
                    lesson.nextReadTime,
                    Modifier.fillMaxWidth(),
                    showClose = false,
                    onClick = { onAction(LessonAction.OpenNext) },
                    thumbnail = { Image(painterResource(lesson.nextThumbnail), null, Modifier.size(64.dp, 80.dp)) },
                )
                Spacer(Modifier.height(spacing.s20))
                Button("Continue later", { onAction(LessonAction.Close) }, Modifier.fillMaxWidth(), style = ButtonStyle.Secondary)
            }
        }
        Confetti(Modifier.fillMaxSize())
    }
}

private const val ConfettiSeed = 48_490_884
private const val ConfettiCount = 90
// in screen heights: past the bottom edge by more than the largest rotated piece
private const val ConfettiExit = 1.1f

private class ConfettiPiece(random: Random) {
    val x = random.nextFloat()
    val y = -0.7f + random.nextFloat() * 0.75f
    val fall = 0.18f + random.nextFloat() * 0.3f
    val sway = 0.01f + random.nextFloat() * 0.03f
    val swayRate = 1f + random.nextFloat() * 2f
    val phase = random.nextFloat() * 2 * PI.toFloat()
    val spin = (random.nextFloat() - 0.5f) * 540f
    val angle = random.nextFloat() * 360f
    val size = 4f + random.nextFloat() * 6f
    val shape = random.nextInt(4)
    val tone = random.nextInt(3)
    val exitMillis = ((ConfettiExit - y) / fall * 1000).toLong()
}

/** A seeded burst of falling confetti; the same clock time always draws the same frame. */
@Composable
private fun Confetti(modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val tones = listOf(colors.brandPrimary, colors.orbViolet, colors.surfaceMethodOrange)
    val pieces = remember { Random(ConfettiSeed).let { r -> List(ConfettiCount) { ConfettiPiece(r) } } }
    val endMillis = remember(pieces) { pieces.maxOf { it.exitMillis } }
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (elapsed < endMillis) withFrameMillis { elapsed = it - start }
    }
    Canvas(modifier) {
        val t = elapsed / 1000f
        // the widest piece (a 20-long strip) reaches 10 from its centre at any angle
        val margin = 10.dp.toPx()
        pieces.forEach { p ->
            val y = (p.y + p.fall * t) * size.height
            if (y < -margin || y > size.height + margin) return@forEach
            val x = (p.x + p.sway * sin(p.phase + p.swayRate * t)) * size.width
            val s = p.size.dp.toPx()
            val color: Color = tones[p.tone]
            rotate(p.angle + p.spin * t, Offset(x, y)) {
                when (p.shape) {
                    0 -> drawRect(color, Offset(x - s / 2, y - s / 2), Size(s, s))
                    1 -> drawCircle(color, s / 2, Offset(x, y))
                    2 -> drawRect(color, Offset(x - s, y - s / 6), Size(2 * s, s / 3))
                    else -> drawPath(
                        Path().apply {
                            moveTo(x, y - s / 2)
                            lineTo(x + s / 2, y + s / 2)
                            lineTo(x - s / 2, y + s / 2)
                            close()
                        },
                        color,
                    )
                }
            }
        }
    }
}
