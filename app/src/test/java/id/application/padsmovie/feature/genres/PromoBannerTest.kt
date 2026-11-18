package id.application.padsmovie.feature.genres

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PromoBannerTest {
    private val image = "https://image.tmdb.org/t/p/w780/sample.jpg"

    @Test
    fun enabledBannerUsesConfiguredMovie() {
        assertEquals(PromoBanner("Featured", image, 42), PromoBanner.fromConfig(true, " Featured ", image, 42))
    }

    @Test
    fun disabledOrIncompleteBannerStaysHidden() {
        assertNull(PromoBanner.fromConfig(false, "Featured", image, 42))
        assertNull(PromoBanner.fromConfig(true, " ", image, 42))
        assertNull(PromoBanner.fromConfig(true, "Featured", "", 42))
        assertNull(PromoBanner.fromConfig(true, "Featured", image, 0))
        assertNull(PromoBanner.fromConfig(true, "Featured", image, Long.MAX_VALUE))
    }

    @Test
    fun bannerRequiresAnHttpsImage() {
        assertNull(PromoBanner.fromConfig(true, "Featured", "http://example.com/poster.jpg", 42))
        assertNull(PromoBanner.fromConfig(true, "Featured", "https:/poster.jpg", 42))
    }
}
