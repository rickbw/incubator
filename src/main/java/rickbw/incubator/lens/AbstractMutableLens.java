package rickbw.incubator.lens;



/**
 * A base class for {@link Lens}es of containers that are mutable. This class
 * uses a Template Method pattern to isolate a deep-copying step and an update
 * step.
 */
public abstract class AbstractMutableLens<T, M> implements Lens<T, M> {

    @Override
    public final T set(final T container, final M member) {
        final T cloned = deepCopy(container);
        modifyCopy(container, member);
        return cloned;
    }

    /**
     * Update the state of the given container, which is a clone of the one
     * passed into {@link #set(Object, Object)}.
     */
    protected abstract void modifyCopy(T copiedContainer, M newMember);

    /**
     * @return  a deep copy of the given object, created in a manner specific
     *          to the concrete subclass of this class.
     */
    protected abstract T deepCopy(final T input);

}
