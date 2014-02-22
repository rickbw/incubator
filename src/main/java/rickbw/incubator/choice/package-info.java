/**
 * The types in this package all deal with <em>exclusive choices</em>: objects
 * that represent zero or one objects out of a set of possibilities, similar
 * to a union in C or C++. They expand upon Guava's
 * {@link com.google.common.base.Optional} in both directions:
 * {@link rickbw.incubator.choice.Nothing} represents a trivial non-existent
 * choice; {@link rickbw.incubator.choice.Either} represents a choice between
 * two possibilities. Null values are not allowed, as they indicate a missing
 * value -- in other words, another contingency.
 *
 * An exclusive choice allows application code to cope with multiple
 * method-return contingencies in a type-safe way without resorting to checked
 * exceptions, which are quite expensive: instances cannot be easily shared,
 * they must be thrown and caught (which is more expensive than returning),
 * and they carry with them stack trace and cause information, which are
 * irrelevant to the task of representing contingencies and yet take time to
 * fill in and space to store.
 */
package rickbw.incubator.choice;
