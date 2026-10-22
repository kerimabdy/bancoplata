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
import tech.bashga.plata.navigation.PaymentsTab
import tech.bashga.plata.screenshots.PLATA_DEVICE
import kotlin.test.assertEquals

/**
 * SCREEN-MAP §5.16, Adding to favorites: Payments → Favorites ⟨AddFavorite⟩ → TransferRecipient(AddFavorite) →
 * Payments ⟨FavoriteAdded⟩.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PLATA_DEVICE)
class AddingToFavoritesFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val app = PaymentsDriver(rule)

    @Test
    fun aPlataCardIsPrependedToFavorites() {
        val repository = AppRepository.inMemory(LiveSnapshot.copy(favorites = emptyList()))
        app.launch(repository, PaymentsTab)

        app.waitFor("Favorites, bills, providers")
        rule.onNode(hasText("Edit")).assertDoesNotExist()
        app.tap("Add")
        app.waitFor("Name, provider, reference")
        repository.update { it.copy(favorites = listOf(Favorite.MyMobile)) }
        app.waitFor("My mobile")

        app.tap("Add to Favorites")
        app.waitFor("By CLABE or card number")
        app.tap("Transfer")
        app.waitFor("By card number")
        app.type("number", "4152313812345678")
        app.waitFor("Plata")
        app.waitFor("Ana López")
        app.tap("Add to Favorites")

        app.waitFor("You added Ana López to Favorites")
        val favorites = repository.snapshot.favorites
        assertEquals(listOf(FavoriteKind.PlataCard, FavoriteKind.Mobile), favorites.map { it.kind })
        assertEquals("4152313812345678", favorites.first().number)
        app.tap("Done")
        app.waitGone("You added Ana López to Favorites")

        DevActions(repository).setMonth(Month.July)
        DevActions(repository).supportMessage()
        repository.update { it.copy(favorites = it.favorites.reversed()) }
        app.waitFor("1,500")
        app.assertLeftToRight("My mobile", "Ana López", "Add")
    }
}
