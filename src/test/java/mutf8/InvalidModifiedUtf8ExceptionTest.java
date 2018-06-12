/*
 * This is free and unencumbered software released into the public domain.
 * For more information, please refer to <http://unlicense.org/>.
 */
package mutf8;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.MalformedInputException;

import static org.junit.jupiter.api.Assertions.*;

class InvalidModifiedUtf8ExceptionTest {

    InvalidModifiedUtf8Exception exception;

    @Nested
    class Constructor {

        @Test
        void nullCause() {
            assertNull(new InvalidModifiedUtf8Exception(null).getCause());
        }

        @Test
        void nonNullCause() {
            MalformedInputException cause = new MalformedInputException(0);
            assertSame(cause, new InvalidModifiedUtf8Exception(cause).getCause());
        }
    }

    @Nested
    class MethodInitCause {

        @BeforeEach
        void newException() {
            exception = new InvalidModifiedUtf8Exception(new MalformedInputException(0));
        }

        @Test
        void nullCause() {
            assertThrows(RuntimeException.class, () -> exception.initCause(null));
        }

        @Test
        void nonNullCause() {
            assertThrows(IllegalStateException.class, () -> exception.initCause(new Throwable()));
        }
    }

    @Nested
    class FillInStackTrace {

        @BeforeEach
        void newException() {
            exception = new InvalidModifiedUtf8Exception(new MalformedInputException(0));
        }

        @Test
        void vanilla() {
            exception.fillInStackTrace();
        }
    }
}
