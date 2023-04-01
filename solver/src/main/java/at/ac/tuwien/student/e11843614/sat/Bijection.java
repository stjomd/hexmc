package at.ac.tuwien.student.e11843614.sat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A data structure that represents a bijection, i.e. a one-to-one correspondence between two objects.
 * Order of the type parameters is irrelevant.
 * @param <S> class of the domain type. 
 * @param <T> class of the image type.
 */
public class Bijection<S, T> {
    
    private final Map<S, T> forward = new HashMap<>();
    private final Map<T, S> backward = new HashMap<>();

    /**
     * Adds a pair of elements to the bijection.
     * @param a an element.
     * @param b an element.
     */
    public void put(S a, T b) {
        forward.put(a, b);
        backward.put(b, a);
    }

    /**
     * Retrieves the element that the specified element is mapped to.
     * @param a an element.
     * @return the element mapped to a.
     */
    public S getFromDomain(T a) {
        return backward.get(a);
    }

    /**
     * Retrieves the element that the specified element is mapped to.
     * @param a an element.
     * @return the element mapped to a.
     */
    public T getFromDestination(S a) {
        return forward.get(a);
    }

    /**
     * Returns the domain set of this bijection.
     * @return the domain.
     */
    public Collection<S> getDomain() {
        return forward.keySet();
    }

    /**
     * Returns the destination (codomain) set of this bijection.
     * @return the destination (codomain).
     */
    public Collection<T> getDestination() {
        return forward.values();
    }

    /**
     * Returns the amount of maps in this bijection, irrespective of direction. That is, a mapping 1 <-> 2 amounts to
     * one map.
     * @return the size.
     */
    public int size() {
        return forward.size();
    }

}
