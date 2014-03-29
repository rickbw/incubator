/* Copyright 2014 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rickbw.incubator.lines;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;


/**
 * An {@link Iterable} backed by lines of text from a {@link Reader}. The
 * {@link Iterator}s returned by this Iterable will be of type
 * {@link LineIterator}.
 *
 * Here's an example:
 * <code>
 *  File myTextFile = new File(myFilePath);
 *  for (String line : LineIterable.fromFile(myTextTile)) {
 *      // ...do cool stuff
 *  }
 * </code>
 *
 * @see #from(Supplier)
 * @see #fromFile(File)
 */
public final class LineIterable implements Iterable<String> {

    private final Supplier<? extends Reader> readerProvider;


    /**
     * {@link Supplier#get()} will be called once for each call to
     * {@link #iterator()}, and so it must return a new {@link Reader} with
     * each call. If the given {@link Supplier} does <em>not</em> return a new
     * {@code Reader} with each call, then the {@code Iterator}s will not be
     * independent.
     *
     * @see #fromCallable(Callable)
     * @see #fromFile(File)
     * @see #fromUrl(URL)
     */
    public static LineIterable from(final Supplier<? extends Reader> readerProvider) {
        return new LineIterable(readerProvider);
    }

    /**
     * {@link Callable#call()} will be called once for each call to
     * {@link #iterator()}, and so it must return a new {@link Reader} with
     * each call. If the given {@link Callable} does <em>not</em> return a new
     * {@code Reader} with each call, then the {@code Iterator}s will not be
     * independent.
     *
     * @see #from(Supplier)
     */
    public static LineIterable fromCallable(final Callable<? extends Reader> provider) {
        Preconditions.checkNotNull(provider);
        final Supplier<Reader> supplier = new Supplier<Reader>() {
            @Override
            public Reader get() {
                try {
                    return provider.call();
                } catch (final RuntimeException rex) {
                    throw rex;
                } catch (final Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        };
        return new LineIterable(supplier);
    }

    /**
     * The given file will be opened for reading with each call to
     * {@link #iterator()}.
     *
     * @see #from(Supplier)
     */
    public static LineIterable fromFile(final File file) {
        Preconditions.checkNotNull(file);
        final Supplier<Reader> supplier = new Supplier<Reader>() {
            @Override
            public Reader get() {
                try {
                    return new FileReader(file);
                } catch (final FileNotFoundException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        };
        return new LineIterable(supplier);
    }

    /**
     * The contents of the given URL will be downloaded with each call to
     * {@link #iterator()}, as with {@link URL#openStream()}.
     *
     * @see #from(Supplier)
     */
    public static LineIterable fromUrl(final URL url) {
        Preconditions.checkNotNull(url);
        final Supplier<Reader> supplier = new Supplier<Reader>() {
            @Override
            public Reader get() {
                try {
                    return new InputStreamReader(url.openStream());
                } catch (final IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        };
        return new LineIterable(supplier);
    }

    @Override
    public LineIterator iterator() {
        return LineIterator.iterator(this.readerProvider.get());
    }

    private LineIterable(final Supplier<? extends Reader> readerProvider) {
        this.readerProvider = Preconditions.checkNotNull(readerProvider);
    }

}
