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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;


/**
 * An {@link Iterator} over the lines in a {@link BufferedReader}. (In Java 8,
 * this capability will be built into the JDK.) The reader may be closed
 * explicitly by calling {@link #close()}. It will be closed implicitly when
 * the input is fully consumed -- that is, once {@link #hasNext()} returns
 * false. The final null returned by {@link BufferedReader#readLine()} is
 * <em>not</em> considered an element by this iterator, and will not be
 * returned.
 *
 * Here's an example:
 * <code>
 *  try (LineIterator it = LineIterator.iterator(myInputStream)) {
 *      // Wrapping in try-with-resources, or closing explicitly at all, is
 *      // optional. The iterator will close itself when !hasNext().
 *      while (it.hasNext()) {
 *          String line = it.next();
 *          // ...do cool stuff
 *      }
 *  }
 * </code>
 *
 * If you need the ability to restart iteration, use {@link LineIterable},
 * which wraps this class.
 *
 * @see #iterator(Reader)
 */
public final class LineIterator
extends AbstractIterator<String>
implements Iterator<String>, Closeable {

    private final BufferedReader reader;


    /**
     * @see #iteratorFromStream(InputStream)
     */
    public static LineIterator iterator(final Reader reader) {
        return new LineIterator(reader);
    }

    /**
     * @see #iterator(Reader)
     */
    public static LineIterator iteratorFromStream(final InputStream stream ) {
        return new LineIterator(new InputStreamReader(stream));
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    @Override
    protected String computeNext() {
        try {
            final String line = this.reader.readLine();
            if (line == null) {
                endOfData();
                close();
            }
            return line;
        } catch (final IOException iox) {
            throw new IllegalStateException(iox);
        }
    }

    private LineIterator(final Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

}
