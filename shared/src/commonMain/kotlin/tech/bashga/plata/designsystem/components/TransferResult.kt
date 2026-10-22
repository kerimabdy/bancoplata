package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_plata_inversiones
import plata.shared.generated.resources.raster_logo_santander
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** A before → after pair shown with [ValueChange]. */
data class BalanceChange(val from: String, val to: String)

private val NameToAccount = 5.dp
private val ChangeGap = 3.dp
private val AccountToAmount = 38.dp
private val AmountToCaption = 5.dp
private val CaptionToSource = 39.dp
private val ChangeToButton = 38.dp
private val BetweenLogoToName = 27.dp
private val BetweenChangeToAmount = 41.dp
private val BetweenAmountToSource = 35.dp

/**
 * Centred transfer result (372e6fd9): [logo] (BrandLogo 96 with the check), [name] 20 bold, [account] 17 secondary,
 * [amount] 36 bold, [caption], the [source] account 20 bold over its [sourceChange], and the white Receipt Button when
 * [onReceipt] is set. Between own accounts (705c4f5e) pass [nameChange] instead of [account] and no caption or receipt.
 * Gaps are measured per block, not snapped to the grid.
 */
@Composable
fun TransferResult(
    name: String,
    amount: String,
    source: String,
    sourceChange: BalanceChange,
    modifier: Modifier = Modifier,
    account: String? = null,
    showAccount: Boolean = account != null,
    nameChange: BalanceChange? = null,
    caption: String? = null,
    onReceipt: (() -> Unit)? = null,
    logo: @Composable () -> Unit,
) {
    val colors = PlataTheme.colors
    val type = PlataTheme.typography
    val between = nameChange != null
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        logo()
        Spacer(Modifier.height(if (between) BetweenLogoToName else PlataTheme.spacing.s24))
        PlataText(name, type.title3, colors.textPrimary, textAlign = TextAlign.Center)
        if (showAccount && account != null) {
            Spacer(Modifier.height(NameToAccount))
            PlataText(account, type.body, colors.textSecondary, textAlign = TextAlign.Center)
        }
        if (nameChange != null) {
            Spacer(Modifier.height(ChangeGap))
            ValueChange(nameChange.from, nameChange.to)
        }
        Spacer(Modifier.height(if (between) BetweenChangeToAmount else AccountToAmount))
        PlataText(amount, type.amountDisplay, colors.textPrimary, maxLines = 1)
        if (caption != null) {
            Spacer(Modifier.height(AmountToCaption))
            PlataText(caption, type.body, colors.textSecondary, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(if (between && caption == null) BetweenAmountToSource else CaptionToSource))
        PlataText(source, type.title3, colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(ChangeGap))
        ValueChange(sourceChange.from, sourceChange.to)
        if (onReceipt != null) {
            Spacer(Modifier.height(ChangeToButton))
            Button("Receipt", onReceipt, style = ButtonStyle.White, icon = PlataIcons.Receipt, iconSize = PlataTheme.sizes.iconLarge)
        }
    }
}

@Preview
@Composable
internal fun TransferResultPreview() = PlataTheme {
    TransferResult(
        name = "Ana López",
        amount = "–$70",
        source = "From Plata Crédito",
        sourceChange = BalanceChange("$78.81", "$8.81"),
        modifier = Modifier.width(393.dp),
        account = "Santander •• 4821",
        caption = "Commission up to $230.84",
        onReceipt = {},
    ) { BrandLogo(painterResource(Res.drawable.raster_logo_santander), BrandLogoSize.S96, showCheck = true) }
}

@Preview
@Composable
internal fun TransferResultBetweenAccountsPreview() = PlataTheme {
    TransferResult(
        name = "Plata Inversiones",
        amount = "$18.76",
        source = "From Plata Crédito",
        sourceChange = BalanceChange("$97.57", "$78.81"),
        modifier = Modifier.width(393.dp),
        nameChange = BalanceChange("USD 0", "USD 1"),
    ) { BrandLogo(painterResource(Res.drawable.raster_logo_plata_inversiones), BrandLogoSize.S96, showCheck = true) }
}
