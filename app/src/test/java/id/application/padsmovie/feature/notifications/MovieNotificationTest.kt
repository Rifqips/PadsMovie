package id.application.padsmovie.feature.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieNotificationTest {
    @Test
    fun acceptsPositiveMovieId() {
        assertEquals(42, movieIdFrom("42"))
    }

    @Test
    fun discardsMissingMalformedAndOutOfRangeIds() {
        listOf(null, "", "abc", "0", "-1", "2147483648", "42/reviews").forEach {
            assertNull(movieIdFrom(it))
        }
    }
}
