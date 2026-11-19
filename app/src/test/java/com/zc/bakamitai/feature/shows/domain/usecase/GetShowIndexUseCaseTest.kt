package com.zc.bakamitai.feature.shows.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.shows.domain.model.ShowIndex
import com.zc.bakamitai.feature.shows.domain.model.ShowIndexSection
import com.zc.bakamitai.feature.shows.domain.repository.FakeShowCatalogRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetShowIndexUseCaseTest {

    private val repository = FakeShowCatalogRepository()
    private val useCase = GetShowIndexUseCase(repository)

    private suspend fun indexOf(vararg titles: String): ShowIndex {
        repository.result = Result.success(titles.map { show(slug = it.lowercase(), title = it) })
        return useCase().getOrThrow()
    }

    private fun ShowIndex.titles(letter: Char) = sections.single { it.letter == letter }.shows.map { it.title }

    @Test
    fun `titles are sorted ignoring case`() = runTest {
        val index = indexOf("Monogatari Series - Off & Monster Season", "mono", "Made in Abyss")

        assertEquals(listOf("Made in Abyss", "mono", "Monogatari Series - Off & Monster Season"), index.titles('M'))
    }

    @Test
    fun `the app's order replaces the page's`() = runTest {
        // The page lists "D_Cide…" after "Dungeon no Naka no Hito".
        val index = indexOf("Dungeon no Naka no Hito", "D_Cide Traumerei the Animation", "Dandadan")

        assertEquals(
            listOf("D_Cide Traumerei the Animation", "Dandadan", "Dungeon no Naka no Hito"),
            index.titles('D'),
        )
    }

    @Test
    fun `digits, symbols and non-ASCII initials go under # first`() = runTest {
        val index = indexOf(
            "86 - Eighty Six",
            "Ōkami",
            "2.43 - Seiin Koukou Danshi Volley-bu",
            "Akame ga Kill!",
            "[Oshi no Ko]",
            "100-man no Inochi no Ue ni Ore wa Tatte Iru",
            "16bit Sensation - Another Layer",
        )

        assertEquals(listOf(ShowIndexSection.OTHER, 'A'), index.sections.map { it.letter })
        assertEquals(
            listOf(
                "100-man no Inochi no Ue ni Ore wa Tatte Iru",
                "16bit Sensation - Another Layer",
                "2.43 - Seiin Koukou Danshi Volley-bu",
                "86 - Eighty Six",
                "[Oshi no Ko]",
                "Ōkami",
            ),
            index.titles(ShowIndexSection.OTHER),
        )
    }

    @Test
    fun `only letters with shows get a section, in order`() = runTest {
        val index = indexOf("Zom 100", "bleach", "Akame ga Kill!", "Blue Lock")

        assertEquals(listOf('A', 'B', 'Z'), index.sections.map { it.letter })
        assertEquals(listOf("bleach", "Blue Lock"), index.titles('B'))
        assertEquals(4, index.showCount)
    }

    @Test
    fun `exact duplicates are removed, other titles of a slug are kept`() = runTest {
        repository.result = Result.success(
            listOf(
                show("fruits-basket-2019", "Fruits Basket (2019) S2"),
                show("fruits-basket-2019", "Fruits Basket (2019)"),
                show("fruits-basket-2019", "Fruits Basket (2019) S3"),
                show("fruits-basket-2019", "Fruits Basket (2019)"),
                show("other-slug", "Fruits Basket (2019)"),
            ),
        )

        val shows = useCase().getOrThrow().sections.single().shows

        assertEquals(
            listOf(
                "fruits-basket-2019" to "Fruits Basket (2019)",
                "other-slug" to "Fruits Basket (2019)",
                "fruits-basket-2019" to "Fruits Basket (2019) S2",
                "fruits-basket-2019" to "Fruits Basket (2019) S3",
            ),
            shows.map { it.slug.value to it.title },
        )
    }

    @Test
    fun `titles that differ only in case keep a stable order`() = runTest {
        val index = indexOf("mono", "Mono", "MONO")

        assertEquals(listOf("MONO", "Mono", "mono"), index.titles('M'))
    }

    @Test
    fun `forceRefresh is passed through`() = runTest {
        useCase()
        useCase(forceRefresh = true)

        assertEquals(listOf(false, true), repository.forceRefreshCalls)
    }

    @Test
    fun `a failure is passed through`() = runTest {
        repository.result = Result.failure(DomainError.Network)

        assertEquals(DomainError.Network, useCase().exceptionOrNull())
    }
}
