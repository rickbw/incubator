package rickbw.incubator.token;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;


/**
 * A base class for types that are "string-ish": typically represented as
 * Strings, but with specific validation requirements, and for which textual
 * operations (e.g. trimming, case conversion, etc.) do not apply. A String
 * ID is a common example.
 *
 * Subclasses should be final or abstract: {@link #equals(Object)} will treat
 * objects of different concrete classes as unequal. Subclasses should also
 * be immutable and should not introduce additional state.
 *
 * @param <SELF> The concrete subclass. This generic parameter allows us to
 *        restrict comparisons to objects of the same concrete type.
 */
public abstract class Stringish<SELF> implements Comparable<Stringish<SELF>>, Serializable {

    private static final long serialVersionUID = -2837809421644307920L;

    private static final Comparator<String> caseSensitiveComparator = new Comparator<String>() {
        @Override
        public int compare(final String lhs, final String rhs) {
            return lhs.compareTo(rhs);
        }
    };

    private static final Comparator<String> notComparableComparator = new Comparator<String>() {
        @Override
        public int compare(final String lhs, final String rhs) {
            throw new UnsupportedOperationException();
        }
    };

    private final String value;
    // TODO: Make this transient and lazy, as in Intish:
    private final Behavior<SELF> behavior;


    /**
     * @param behavior A Strategy object used to validate and compare objects.
     *        Subclasses should generally maintain a private static final
     *        instance and pass it consistently.
     *
     * @see #behavior()
     */
    protected Stringish(final Behavior<SELF> behavior, final String value) {
        this.behavior = behavior;
        this.value = this.behavior.validateAndNormalize(value); // throw NPE on null behavior
    }


    /**
     * @see #toString()
     */
    public final String stringValue() {
        return this.value;
    }


    @Override
    public int compareTo(final Stringish<SELF> rhs) {
        return this.behavior.compare(this, rhs);
    }


    /**
     * Determines equality based on the type-specific {@link Comparator}.
     */
    @Override
    public boolean equals(final Object other) {
        if (null == other) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final Stringish<SELF> otherStringish = (Stringish<SELF>) other;
        return compareTo(otherStringish) == 0;
    }


    /**
     * Caveat: Objects that compare as equal should hash as equal. However,
     * this method can't actually determine that. Therefore, it follows this
     * heuristic: if the configured comparator is equal to
     * {@link String#CASE_INSENSITIVE_ORDER}, according to its
     * {@link Object#equals(Object)} method, then the hash result will be
     * case-insensitive. Otherwise, it will be case-sensitive.
     */
    @Override
    public final int hashCode() {
        final String valueToHash;
        if (this.behavior.comparator.equals(String.CASE_INSENSITIVE_ORDER)) {
            valueToHash = this.value.toLowerCase();
        } else {
            valueToHash = this.value;
        }
        return valueToHash.hashCode();
    }


    /**
     * @return A string identifying both the type of this object as well as
     *         its string value.
     *
     * @see #stringValue()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + this.value + ')';
    }


    /**
     * Used by subclasses to customize their validation and comparison
     * behavior.
     */
    protected static BehaviorBuilder behavior() {
        return new BehaviorBuilder();
    }


    /**
     * @return A convenience {@link Comparator}.
     *
     * @see #caseInsensitive()
     * @see Behavior
     */
    protected static Comparator<String> caseSensitive() {
        return caseSensitiveComparator;
    }


    /**
     * @return String#CASE_INSENSITIVE_ORDER, a convenience
     *         {@link Comparator}.
     *
     * @see #caseSensitive()
     * @see Behavior
     */
    protected static Comparator<String> caseInsensitive() {
        return String.CASE_INSENSITIVE_ORDER;
    }


    /**
     * @return A convenience {@link Comparator} that throws
     * {@link UnsupportedOperationException}.
     *
     * @see Behavior
     */
    protected static Comparator<String> notComparable() {
        return notComparableComparator;
    }


    protected static final class BehaviorBuilder {
        private int minLength = 1;
        private int maxLength = Integer.MAX_VALUE;
        private Comparator<String> comparator = caseSensitive();
        private CaseNormalizer caseNormalization = CaseNormalizer.NONE;
        private WhitespaceNormalizer whitespaceNormalization = WhitespaceNormalizer.NONE;

        /**
         * Minimum allowable value, inclusive. Default is 1.
         */
        public BehaviorBuilder minimumLength(final int newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("can't be negative: " + newValue);
            }
            this.minLength = newValue;
            return this;
        }

        /**
         * Maximum allowable value, inclusive. Default is
         * {@link Integer#MAX_VALUE}.
         */
        public BehaviorBuilder maximumLength(final int newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("can't be negative: " + newValue);
            }
            this.maxLength = newValue;
            return this;
        }

        /**
         * Used for comparisons with other {@link Stringish} instances. See
         * {@link Stringish#compareTo(Stringish)} and
         * {@link Stringish#notComparable()}.
         */
        public BehaviorBuilder comparator(final Comparator<String> newComparator) {
            if (null == newComparator) {
                throw new IllegalArgumentException("null comparator");
            }
            this.comparator = newComparator;
            return this;
        }

        /**
         * The default behavior.
         */
        public BehaviorBuilder doNotNormalizeCase() {
            this.caseNormalization = CaseNormalizer.NONE;
            return this;
        }

        public BehaviorBuilder normalizeToLowerCase() {
            this.caseNormalization = CaseNormalizer.TO_LOWER_CASE;
            return this;
        }

        public BehaviorBuilder normalizeToUpperCase() {
            this.caseNormalization = CaseNormalizer.TO_UPPER_CASE;
            return this;
        }

        /**
         * The default behavior.
         */
        public BehaviorBuilder doNotTrimWhitespace() {
            this.whitespaceNormalization = WhitespaceNormalizer.NONE;
            return this;
        }

        public BehaviorBuilder trimWhitespace() {
            this.whitespaceNormalization = WhitespaceNormalizer.TRIM;
            return this;
        }

        public <SELF> Behavior<SELF> build() {
            return new Behavior<SELF>(
                    this.minLength,
                    this.maxLength,
                    this.comparator,
                    Arrays.asList(new StringNormalizer[] {
                            this.caseNormalization,
                            this.whitespaceNormalization
                    }));
        }
    }


    protected static final class Behavior<SELF> implements Comparator<Stringish<SELF>> {
        private final int minLength;
        private final int maxLength;
        private final Comparator<String> comparator;
        private final Iterable<StringNormalizer> normalizers;

        private Behavior(final int minLength, final int maxLength, final Comparator<String> comparator, final Iterable<StringNormalizer> normalizers) {
            assert minLength >= 0 : minLength;
            if (maxLength < minLength) {
                throw new IllegalArgumentException("max " + maxLength + " < " + " min " + minLength);
            }
            this.minLength = minLength;
            this.maxLength = maxLength;

            if (null == comparator) {
                throw new IllegalArgumentException("null comparator");
            }
            this.comparator = comparator;

            if (null == normalizers) {
                throw new IllegalArgumentException("null case normalization");
            }
            this.normalizers = normalizers;
        }

        private String validateAndNormalize(String value) {
            // --- Validate --- //
            final int length = value.length(); // throw NPE on null
            if (length < this.minLength) {
                throw new IllegalArgumentException("length " + length + " < minimum " + this.minLength);
            }
            if (length > this.maxLength) {
                throw new IllegalArgumentException("length " + length + " > maximum " + this.maxLength);
            }

            // --- Normalize --- //
            for (final StringNormalizer normalizer : this.normalizers) {
                value = normalizer.normalize(value);
            }
            return value;
        }

        @Override
        public int compare(final Stringish<SELF> lhs, final Stringish<SELF> rhs) {
            return this.comparator.compare(lhs.value, rhs.value);
        }
    }


    private interface StringNormalizer {
        public String normalize(String value);
    }


    private static enum CaseNormalizer implements StringNormalizer {
        TO_LOWER_CASE {
            @Override
            public String normalize(final String value) {
                return value.toLowerCase();
            }
        },

        TO_UPPER_CASE {
            @Override
            public String normalize(final String value) {
                return value.toUpperCase();
            }
        },

        NONE {
            @Override
            public String normalize(final String value) {
                return value;
            }
        },
    }


    private static enum WhitespaceNormalizer implements StringNormalizer {
        TRIM {
            @Override
            public String normalize(final String value) {
                return value.trim();
            }
        },

        NONE {
            @Override
            public String normalize(final String value) {
                return value;
            }
        },
    }

}
