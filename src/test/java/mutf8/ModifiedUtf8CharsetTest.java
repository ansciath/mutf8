/*
 * This is free and unencumbered software released into the public domain.
 * For more information, please refer to <http://unlicense.org/>,
 */
package mutf8;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ModifiedUtf8CharsetTest {

    ModifiedUtf8Charset charset;

    @Nested
    class Constructor {

        @Test
        void vanilla() {
            new ModifiedUtf8Charset();
        }
    }

    @Nested
    class MethodContains {

        @BeforeEach
        void newCharset() {
            charset = new ModifiedUtf8Charset();
        }

        @Test
        void nullCharset() {
            assertThrows(NullPointerException.class, () -> charset.contains(null));
        }

        @Test
        void sameCharset() {
            assertTrue(charset.contains(charset));
        }

        @Test
        void sameClass() {
            assertTrue(charset.contains(new ModifiedUtf8Charset()));
        }

        @Test
        void utf8() {
            assertTrue(charset.contains(StandardCharsets.UTF_8));
        }

        @Test
        void containedByUtf8() {
            assertTrue(StandardCharsets.UTF_8.contains(StandardCharsets.US_ASCII));
            assertTrue(charset.contains(StandardCharsets.US_ASCII));
        }

        @Test
        void notContainedByUtf8() {
            Charset mockCharset = new Charset("mock", null) {
                @Override
                public boolean contains(Charset cs) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public CharsetDecoder newDecoder() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public CharsetEncoder newEncoder() {
                    throw new UnsupportedOperationException();
                }
            };
            assertFalse(StandardCharsets.UTF_8.contains(mockCharset));
            assertFalse(charset.contains(mockCharset));
        }
    }

    @Nested
    class ClassDecoder {

        ModifiedUtf8Charset.Decoder decoder;

        @Nested
        class Constructor {

            @BeforeEach
            void newCharset() {
                charset = new ModifiedUtf8Charset();
            }

            @Test
            void vanilla() {
                ModifiedUtf8Charset.Decoder decoder = new ModifiedUtf8Charset.Decoder(charset);
                assertSame(charset, decoder.charset());
                assertTrue(1.0F >= decoder.averageCharsPerByte());
                assertEquals(1.0F, decoder.maxCharsPerByte());
            }
        }

        @Nested
        class MethodDecodeLoop {

            @BeforeEach
            void newDecoder() {
                charset = new ModifiedUtf8Charset();
                decoder = new ModifiedUtf8Charset.Decoder(charset);
            }

            @Test
            void emptySource() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.UNDERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContainsNullOctet() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { 0x00 });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.UNDERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap("\u0000"), target.flip());
            }

            @Test
            void sourceContainsNullCharacterSequenceAndTargetCanFit0Characters() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xC0), (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(0);
                assertEquals(CoderResult.OVERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0xC0), (byte)(0x80) }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContainsNullCharacterSequenceAndTargetCanFit1Character() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xC0), (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.UNDERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap("\u0000"), target.flip());
            }

            @Test
            void sourceContains1OctetSequenceAndTargetCanFit0Characters() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { 'A' });
                final CharBuffer target = CharBuffer.allocate(0);
                assertEquals(CoderResult.OVERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { 'A' }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContains1OctetSequenceAndTargetCanFit1Character() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { 'A' });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.UNDERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap("A"), target.flip());
            }

            @Test // whether the decoder correctly handles a source that contains a leading octet of the form 10xxxxxx
            void sourceContains1OctetSequenceWithInvalidOctet0() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.malformedForLength(1), decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContains2OctetSequenceAndTargetCanFit0Characters() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xC3), (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(0);
                assertEquals(CoderResult.OVERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0xC3), (byte)(0x80) }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContains2OctetSequenceAndTargetCanFit1Character() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xC3), (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.UNDERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap("\u00C0"), target.flip());
            }

            @Test
            void sourceContains2OctetSequenceWithInvalidOctet1() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xC3), (byte)(0xC0) });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.malformedForLength(2), decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContains3OctetSequenceAndTargetCanFit0Characters() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xE1), (byte)(0xB8), (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(0);
                assertEquals(CoderResult.OVERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0xE1), (byte)(0xB8), (byte)(0x80) }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContains3OctetSequenceAndTargetCanFit1Character() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xE1), (byte)(0xB8), (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.UNDERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap("\u1E00"), target.flip());
            }

            @Test
            void sourceContains3OctetSequenceWithInvalidOctet1() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xE1), (byte)(0xF8), (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.malformedForLength(2), decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0x80) }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContains3OctetSequenceWithInvalidOctet2() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xE1), (byte)(0xB8), (byte)(0xC0) });
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.malformedForLength(3), decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test // whether the decoder correctly handles a source that contains a leading octet of the form 1111xxxx
            void sourceContains4OctetSequence() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xF0), (byte)(0x9D), (byte)(0x90), (byte)(0x80) }); // valid UTF-8
                final CharBuffer target = CharBuffer.allocate(1);
                assertEquals(CoderResult.malformedForLength(1), decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0x9D), (byte)(0x90), (byte)(0x80) }), source);
                assertEquals(CharBuffer.wrap(""), target.flip());
            }

            @Test
            void sourceContainsVariousAndTargetCanFitMore() {
                final ByteBuffer source = ByteBuffer.wrap(new byte[] { (byte)(0xC0), (byte)(0x80), 'A', (byte)(0xC3), (byte)(0x80), (byte)(0xE1), (byte)(0xB8), (byte)(0x80) });
                final CharBuffer target = CharBuffer.allocate(16);
                assertEquals(CoderResult.UNDERFLOW, decoder.decodeLoop(source, target));
                assertEquals(ByteBuffer.wrap(new byte[] { }), source);
                assertEquals(CharBuffer.wrap("\u0000A\u00C0\u1E00"), target.flip());
            }
        }
    }

    @Nested
    class MethodNewDecoder {

        @BeforeEach
        void newCharset() {
            charset = new ModifiedUtf8Charset();
        }

        @Test
        void returnsDistinct() {
            assertNotSame(charset.newDecoder(), charset.newDecoder());
        }
    }

    @Nested
    class ClassEncoder {

        ModifiedUtf8Charset.Encoder encoder;

        @Nested
        class Constructor {

            @BeforeEach
            void newCharset() {
                charset = new ModifiedUtf8Charset();
            }

            @Test
            void vanilla() {
                ModifiedUtf8Charset.Encoder encoder = new ModifiedUtf8Charset.Encoder(charset);
                assertSame(charset, encoder.charset());
                assertTrue(1.0F <= encoder.averageBytesPerChar());
                assertEquals(3.0F, encoder.maxBytesPerChar());
            }
        }

        @Nested
        class MethodEncodeLoop {

            @BeforeEach
            void newEncoder() {
                charset = new ModifiedUtf8Charset();
                encoder = new ModifiedUtf8Charset.Encoder(charset);
            }

            @Test
            void emptySource() {
                final CharBuffer source = CharBuffer.wrap("");
                final ByteBuffer target = ByteBuffer.allocate(1);
                assertEquals(CoderResult.UNDERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap(""), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContainsNullCharacterAndTargetCanFit0Octets() {
                final CharBuffer source = CharBuffer.wrap("\u0000");
                final ByteBuffer target = ByteBuffer.allocate(0);
                assertEquals(CoderResult.OVERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap("\u0000"), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContainsNullCharacterAndTargetCanFit1Octet() {
                final CharBuffer source = CharBuffer.wrap("\u0000");
                final ByteBuffer target = ByteBuffer.allocate(1);
                assertEquals(CoderResult.OVERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap("\u0000"), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContainsNullCharacterAndTargetCanFit2Octets() {
                final CharBuffer source = CharBuffer.wrap("\u0000");
                final ByteBuffer target = ByteBuffer.allocate(2);
                assertEquals(CoderResult.UNDERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap(""), source);
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0xC0), (byte)(0x80) }), target.flip());
            }

            @Test
            void sourceContains1OctetCharacterAndTargetCanFit0Octets() {
                final CharBuffer source = CharBuffer.wrap("A");
                final ByteBuffer target = ByteBuffer.allocate(0);
                assertEquals(CoderResult.OVERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap("A"), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContains1OctetCharacterAndTargetCanFit1Octet() {
                final CharBuffer source = CharBuffer.wrap("A");
                final ByteBuffer target = ByteBuffer.allocate(1);
                assertEquals(CoderResult.UNDERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap(""), source);
                assertEquals(ByteBuffer.wrap(new byte[] { 'A' }), target.flip());
            }

            @Test
            void sourceContains2OctetCharacterAndTargetCanFit0Octets() {
                final CharBuffer source = CharBuffer.wrap("\u00C0");
                final ByteBuffer target = ByteBuffer.allocate(0);
                assertEquals(CoderResult.OVERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap("\u00C0"), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContains2OctetCharacterAndTargetCanFit1Octet() {
                final CharBuffer source = CharBuffer.wrap("\u00C0");
                final ByteBuffer target = ByteBuffer.allocate(1);
                assertEquals(CoderResult.OVERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap("\u00C0"), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContains2OctetCharacterAndTargetCanFit2Octets() {
                final CharBuffer source = CharBuffer.wrap("\u00C0");
                final ByteBuffer target = ByteBuffer.allocate(2);
                assertEquals(CoderResult.UNDERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap(""), source);
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0xC3), (byte)(0x80) }), target.flip());
            }

            @Test
            void sourceContains3OctetCharacterAndTargetCanFit0Octets() {
                final CharBuffer source = CharBuffer.wrap("\u1E00");
                final ByteBuffer target = ByteBuffer.allocate(0);
                assertEquals(CoderResult.OVERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap("\u1E00"), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContains3OctetCharacterAndTargetCanFit1Octet() {
                final CharBuffer source = CharBuffer.wrap("\u1E00");
                final ByteBuffer target = ByteBuffer.allocate(1);
                assertEquals(CoderResult.OVERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap("\u1E00"), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContains3OctetCharacterAndTargetCanFit2Octets() {
                final CharBuffer source = CharBuffer.wrap("\u1E00");
                final ByteBuffer target = ByteBuffer.allocate(2);
                assertEquals(CoderResult.OVERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap("\u1E00"), source);
                assertEquals(ByteBuffer.wrap(new byte[] { }), target.flip());
            }

            @Test
            void sourceContains3OctetCharacterAndTargetCanFit3Octets() {
                final CharBuffer source = CharBuffer.wrap("\u1E00");
                final ByteBuffer target = ByteBuffer.allocate(3);
                assertEquals(CoderResult.UNDERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap(""), source);
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0xE1), (byte)(0xB8), (byte)(0x80) }), target.flip());
            }

            @Test
            void sourceContainsVariousAndTargetCanFitMore() {
                final CharBuffer source = CharBuffer.wrap("\u0000A\u00C0\u1E00");
                final ByteBuffer target = ByteBuffer.allocate(16);
                assertEquals(CoderResult.UNDERFLOW, encoder.encodeLoop(source, target));
                assertEquals(CharBuffer.wrap(""), source);
                assertEquals(ByteBuffer.wrap(new byte[] { (byte)(0xC0), (byte)(0x80), 'A', (byte)(0xC3), (byte)(0x80), (byte)(0xE1), (byte)(0xB8), (byte)(0x80) }), target.flip());
            }
        }
    }

    @Nested
    class MethodNewEncoder {

        @BeforeEach
        void newCharset() {
            charset = new ModifiedUtf8Charset();
        }

        @Test
        void returnsDistinct() {
            assertNotSame(charset.newEncoder(), charset.newEncoder());
        }
    }
}
