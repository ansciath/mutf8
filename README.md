# Modified UTF-8 Charset

A charset for modified UTF-8, the character-encoding scheme used by the Java
class file format for the internal representation of strings.

## FEATURES

  * A charset for modified UTF-8, the character-encoding scheme used by the Java
    class file format for the internal representation of strings. This charset
    supports both encoding and decoding.
  * A charset provider that allows the modified UTF-8 charset to be obtained via
    the charset lookup service defined by the Java SE.
  * An unchecked exception type for use when decoding input that is expected to
    be a valid encoding of modified UTF-8.

## INSTALLING

This module requires the Java SE 7 or later.

The source of this module has been released to the public domain via Unlicense
(see below). The choice of license was specifically intended to make it easier
to incorporate and modify the source.

Otherwise, this module can be packaged as a JAR. For example:

    % cd <mutf8-root-dir>
    % mvn package
    % cp target/mutf8-1.0.jar <project-libs>

To obtain an instance of ModifiedUTF8Charset, do one of the following:

  * Obtain an instance of the charset by using the charset lookup service
    defined by the Java SE (recommended):

        java.nio.charset.Charset.forName("X-MUTF-8");

    This requires the project to be configured to use the included charset
    provider:

      + For the Java SE 8 or earlier, add the following line:

            mutf8.spi.ModifiedUtf8CharsetProvider

        to the project file:

            META-INF/services/java.nio.charset.spi.CharsetProvider

      + For the Java SE 9 or later, when including this module as source, add
        the following line:

            provides java.nio.charset.spi.CharsetProvider with mutf8.spi.ModifiedUtf8CharsetProvider;

        to the module declaration of the project file:

            module-info.java

      + For the Java SE 9 or later, when including this module as a JAR, add the
        following line:

            requires mutf8;

        to the module declaration of the project file:

            module-info.java

 *  Obtain an instance of the charset by directly using the included charset
    provider:

        new mutf8.spi.ModifiedUtf8CharsetProvider().charsetForName("X-MUTF-8");

 *  Obtain an instance of the charset by direct instantiation (not recommended):

        new ModifiedUtf8Charset();


## LINKS

[GitHub](https://github.com/ansciath/mutf8)


## LICENSE

This is free and unencumbered software released into the public domain.
For more information, please refer to [http://unlicense.org]().
