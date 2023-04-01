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
    
    public void put(S a, T b) {
        forward.put(a, b);
        backward.put(b, a);
    }
    
    public S get1(T a) {
        return backward.get(a);
    }
    
    public T get2(S a) {
        return forward.get(a);
    }

    public Collection<S> getDomain() {
        return forward.keySet();
    }

    public Collection<T> getDestination() {
        return forward.values();
    }

}
