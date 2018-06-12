/*
 * This is free and unencumbered software released into the public domain.
 * For more information, please refer to <http://unlicense.org/>.
 */
package mutf8.spi;

import mutf8.ModifiedUtf8Charset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ModifiedUtf8CharsetProviderTest {

    @Test
    void useCharsetLookup() {
        Charset charset = Charset.forName(ModifiedUtf8Charset.CANONICAL_NAME);
        assertTrue(charset instanceof ModifiedUtf8Charset);
    }

    ModifiedUtf8CharsetProvider provider;

    @Nested
    class Constructor {

        @Test
        void vanilla() {
            new ModifiedUtf8CharsetProvider();
        }
    }

    @Nested
    class MethodCharsetForName {

        @BeforeEach
        void newProvider() {
            provider = new ModifiedUtf8CharsetProvider();
        }

        @Test
        void withNullName() {
            assertThrows(NullPointerException.class, () -> provider.charsetForName(null));
        }

        @Test
        void withEmptyName() {
            assertNull(provider.charsetForName(""));
        }

        @Test
        void withIllegalName() {
            assertNull(provider.charsetForName("<illegal|name>"));
        }

        @Test
        void withUnsupportedName() {
            assertNull(provider.charsetForName(StandardCharsets.UTF_8.name()));
        }

        @Test
        void withSupportedName() {
            assertNotNull(provider.charsetForName(ModifiedUtf8Charset.CANONICAL_NAME));
        }
    }

    @Nested
    class MethodCharsets {

        @BeforeEach
        void newProvider() {
            provider = new ModifiedUtf8CharsetProvider();
        }

        @Test
        void returnsSingleton() {
            Iterator<Charset> charsets = provider.charsets();
            assertTrue(charsets.next() instanceof ModifiedUtf8Charset);
            assertFalse(charsets.hasNext());
        }

        @Test
        void returnsDistinct() {
            assertNotSame(provider.charsets(), provider.charsets());
        }

        @Test
        void returnsUnmodifiable() {
            Iterator<Charset> charsets = provider.charsets();
            charsets.next();
            assertThrows(UnsupportedOperationException.class, charsets::remove);
        }
    }
}
