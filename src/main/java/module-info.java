/*
 * This is free and unencumbered software released into the public domain.
 * For more information, please refer to <http://unlicense.org/>.
 */
module mutf8 {
    provides java.nio.charset.spi.CharsetProvider with mutf8.spi.ModifiedUtf8CharsetProvider;
}
