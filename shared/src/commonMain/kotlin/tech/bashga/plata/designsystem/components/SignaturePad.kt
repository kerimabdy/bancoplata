package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.Trash
import tech.bashga.plata.designsystem.icons.Undo
import tech.bashga.plata.designsystem.theme.PlataTheme

/** One pen stroke, its points as fractions of the pad (0–1 on both axes), so a stroke survives any pad size. */
@Immutable
data class SignatureStroke(val points: List<Offset>)

// aee18063: the 285×673 pad at (20, 125); the landscape actions turned upright 17 from the right edge, 9.5 apart
private val PadTop = 56.dp
private val PadWidth = 285.dp
private val PadHeight = 673.dp
private val ActionsTop = 226.dp
private val ActionsEnd = 17.dp
private val ActionGap = 9.5.dp
private val DoneLength = 88.dp
private val ClearLength = 111.5.dp
private val UndoLength = 114.5.dp
private val InkWidth = 3.dp

// lays the child out landscape and turns it a quarter left, as the signature sheet's buttons read bottom to top
private fun Modifier.turnedUp(): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())
    layout(placeable.height, placeable.width) {
        placeable.placeWithLayer((placeable.height - placeable.width) / 2, (placeable.width - placeable.height) / 2) {
            rotationZ = -90f
        }
    }
}

/**
 * The landscape signature sheet's content (aee18063): "Your signature" with Cancel, the bordered pad you draw
 * [strokes] on (each finished drag calls [onStroke]), and the turned Done (enabled once there is a stroke), Clear
 * and Undo. Present it in a Stacked page sheet.
 */
@Composable
fun SignaturePad(
    strokes: List<SignatureStroke>,
    onStroke: (SignatureStroke) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PlataTheme.colors
    val ink = colors.textPrimary
    var current by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val latestOnStroke by rememberUpdatedState(onStroke)
    Box(modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .padding(start = PlataTheme.spacing.screenMargin, top = PadTop)
                .size(PadWidth, PadHeight)
                .border(1.dp, ink, ContinuousRoundedShape(PlataTheme.radii.xl))
                .pointerInput(Unit) {
                    fun Offset.fraction() = Offset(x / size.width, y / size.height)
                    detectDragGestures(
                        onDragStart = { current = listOf(it.fraction()) },
                        onDrag = { change, _ -> current = current + change.position.fraction() },
                        onDragEnd = {
                            if (current.size > 1) latestOnStroke(SignatureStroke(current))
                            current = emptyList()
                        },
                        onDragCancel = { current = emptyList() },
                    )
                },
        ) {
            val style = Stroke(InkWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            (strokes.map { it.points } + listOf(current)).filter { it.size > 1 }.forEach { points ->
                val path = Path().apply {
                    moveTo(points[0].x * size.width, points[0].y * size.height)
                    points.drop(1).forEach { lineTo(it.x * size.width, it.y * size.height) }
                }
                drawPath(path, ink, style = style)
            }
        }
        Column(
            Modifier.align(Alignment.TopEnd).padding(top = ActionsTop, end = ActionsEnd),
            verticalArrangement = Arrangement.spacedBy(ActionGap),
        ) {
            Button("Done", onDone, Modifier.turnedUp().width(DoneLength), enabled = strokes.isNotEmpty())
            Button("Clear", onClear, Modifier.turnedUp().width(ClearLength), style = ButtonStyle.SecondaryOnCard, icon = PlataIcons.Trash)
            Button("Undo", onUndo, Modifier.turnedUp().width(UndoLength), style = ButtonStyle.SecondaryOnCard, icon = PlataIcons.Undo)
        }
        NavigationBar(style = NavigationBarStyle.Sheet, title = "Your signature", trailing = { TextButton("Cancel", onCancel) })
    }
}

internal val SampleSignature = listOf(
    SignatureStroke(listOf(Offset(0.30f, 0.30f), Offset(0.42f, 0.36f), Offset(0.36f, 0.45f), Offset(0.55f, 0.50f), Offset(0.48f, 0.62f), Offset(0.68f, 0.70f))),
)

@Preview
@Composable
internal fun SignaturePadPreview() = PlataTheme {
    ProvideSafeArea(PlataSafeArea.IPhone) {
        Box(Modifier.size(393.dp, 783.dp)) {
            SignaturePad(SampleSignature, {}, {}, {}, {}, {})
        }
    }
}
