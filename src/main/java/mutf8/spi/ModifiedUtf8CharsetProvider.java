/*
 * This is free and unencumbered software released into the public domain.
 * For more information, please refer to <http://unlicense.org/>.
 */
package mutf8.spi;

import mutf8.ModifiedUtf8Charset;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Iterator;

/**
 * A provider for a charset for modified UTF-8, the character-encoding scheme
 * used by the {@code class} file format for the internal representation of
 * strings.
 *
 * @author  Nathan Ryan
 * @see     ModifiedUtf8Charset
 */
public class ModifiedUtf8CharsetProvider extends CharsetProvider {

    private static final ModifiedUtf8Charset MUTF_8 = new ModifiedUtf8Charset();

    @Override
    public ModifiedUtf8Charset charsetForName(final String charsetName) {
        return (charsetName.equals(MUTF_8.name()) ? MUTF_8 : null);
    }

    @Override
    public Iterator<Charset> charsets() {
        return Collections.<Charset>singleton(MUTF_8).iterator();
    }
}
