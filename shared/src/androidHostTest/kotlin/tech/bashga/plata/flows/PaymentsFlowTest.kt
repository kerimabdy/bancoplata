package tech.bashga.plata.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.Favorite
import tech.bashga.plata.data.FavoriteKind
import tech.bashga.plata.data.Month
import tech.bashga.plata.dev.DevActions
import tech.bashga.plata.navigation.HomeTab
import tech.bashga.plata.screenshots.PLATA_DEVICE

/** SCREEN-MAP §5.34, Payments: Home → the Payments tab, before and after flow 16's favorite. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class PaymentsFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val app = PaymentsDriver(rule)

    @Test
    fun thePaymentsTabShowsTheSavedFavorites() {
        val repository = AppRepository.inMemory(LiveSnapshot.copy(favorites = emptyList()))
        app.launch(repository, HomeTab)

        app.tap("Payments")
        app.waitFor("Favorites, bills, providers")
        app.waitFor("Add")
        rule.onNode(hasText("Edit")).assertDoesNotExist()
        app.waitFor("$200")

        val card = Favorite("platacard-4152313812345678", FavoriteKind.PlataCard, "Ana López", "4152313812345678")
        repository.update { it.copy(favorites = listOf(Favorite.MyMobile, card)) }
        DevActions(repository).setMonth(Month.July)
        DevActions(repository).supportMessage()
        app.waitFor("Edit")
        app.waitFor("1,500")
        app.assertLeftToRight("My mobile", "Ana López", "Add")
    }
}
