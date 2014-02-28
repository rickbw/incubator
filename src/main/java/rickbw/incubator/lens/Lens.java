package rickbw.incubator.lens;


/**
 * A Lens provides a pseudo-mutable view on a possibly-immutable data
 * structure. It encapsulates a particular member (of type <code>M</code>) of
 * a given class of aggregate data structure (of type <code>T</code>) -- for
 * example, a particular property of a Java Bean.
 *
 * @param <T>   The Type of the containing object.
 * @param <M>   The type of the Member of the containing object, the value of
 *              which can be retrieved and set.
 */
public interface Lens<T, M> {

    /**
     * @return  the value of the encapsulated member from the given container.
     *
     * @throws  UnsupportedOperationException   if the member is write-only.
     */
    M get(T container);

    /**
     * "Set" the value of the encapsulated member to the given new value by
     * returning a container object that reflects the change. Subsequently
     * passing this method's result to {@link #get(Object)} will result in a
     * value equal to the given new member value.
     *
     * The given container object <em>should</em> be treated as immutable, and
     * implementations should return a copy with the member update applied.
     * However, special-purpose Lens implementations may actually modify the
     * input container directly.
     *
     * @return  a copy of the given container reflecting the given change, or
     *          a reference to the mutated container, as appropriate.
     *
     * @throws  UnsupportedOperationException   if the member is read-only.
     */
    T set(T container, M member);

}
