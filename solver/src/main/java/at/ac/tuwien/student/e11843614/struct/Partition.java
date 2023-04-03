package at.ac.tuwien.student.e11843614.struct;

import java.util.HashSet;
import java.util.Set;

/**
 * An object that represents a partition, i.e. a set of disjoint sets called equivalence classes.
 * @param <T> the class of the objects stored in the equivalence classes.
 */
public class Partition<T> {

    private final Set<Set<T>> equivalenceClasses = new HashSet<>();

    /**
     * Adds an element to this partition. If the element was not present in any equivalence class, creates a new
     * equivalence class. Otherwise, does nothing.
     * @param a the element to be added.
     */
    public void add(T a) {
        for (Set<T> equivalenceClass : equivalenceClasses) {
            if (equivalenceClass.contains(a)) {
                return;
            }
        }
        Set<T> newEc = new HashSet<>();
        newEc.add(a);
        equivalenceClasses.add(newEc);
    }

    /**
     * Adds elements to this partition. If there is an equivalence class that contains one element, adds the other to
     * that equivalence class. Otherwise creates a new equivalence class. Also ensures that all equivalence classes are
     * disjoint.
     * @param a an element.
     * @param b an element.
     */
    public void add(T a, T b) {
        boolean added = false;
        for (Set<T> equivalenceClass : equivalenceClasses) {
            // When adding new elements, hashCode change in the inner set causes bugs in the outer set. Therefore, we
            // have to create new sets all the time, unfortunately.
            if (equivalenceClass.contains(b)) {
                extendEquivalenceClassWith(equivalenceClass, a);
                added = true;
                break;
            } else if (equivalenceClass.contains(a)) {
                extendEquivalenceClassWith(equivalenceClass, b);
                added = true;
                break;
            }
        }
        // If element was not added, create a new equivalence class.
        if (!added) {
            Set<T> newEquivalenceClass = new HashSet<>();
            newEquivalenceClass.add(a);
            newEquivalenceClass.add(b);
            equivalenceClasses.add(newEquivalenceClass);
        }
        // If duplicates appeared, merge the equivalence classes.
        mergeDuplicates(a);
        mergeDuplicates(b);
    }

    /**
     * Returns the set of equivalence classes in this partition.
     * @return the set of equivalence classes.
     */
    public Set<Set<T>> getEquivalenceClasses() {
        return equivalenceClasses;
    }

    /**
     * Returns the amount of equivalence classes in this partition.
     * @return the size of this partition.
     */
    public int size() {
        return equivalenceClasses.size();
    }

    /**
     * Checks if this partition is a refinement of another partition.
     * @param p a partition.
     * @return true, if this partition is a refinement of p, and false otherwise.
     */
    public boolean isRefinementOf(Partition<T> p) {
        for (Set<T> ecThis : equivalenceClasses) {
            boolean found = false;
            for (Set<T> ecP : p.equivalenceClasses) {
                if (ecP.containsAll(ecThis)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    // ----- Helpers ---------------------------------------------------------------------------------------------------

    /**
     * Replaces the specified equivalence class with a new one that contains all elements from the specified one plus
     * one element.
     * @param ec the equivalence class to be replaced.
     * @param a the new element.
     */
    private void extendEquivalenceClassWith(Set<T> ec, T a) {
        Set<T> newEc = new HashSet<>(ec);
        newEc.add(a);
        equivalenceClasses.remove(ec);
        equivalenceClasses.add(newEc);
    }

    /**
     * Merges equivalence classes that contain a specified element together.
     * @param element the element to check equivalence classes against.
     */
    private void mergeDuplicates(T element) {
        // Store equivalence classes with duplicates
        Set<Set<T>> ecWithElement = new HashSet<>();
        for (Set<T> ec : equivalenceClasses) {
            if (ec.contains(element))
                ecWithElement.add(ec);
        }
        if (ecWithElement.size() > 1) {
            // Create the merged equivalence class
            Set<T> newEc = new HashSet<>();
            for (Set<T> ec : ecWithElement) {
                newEc.addAll(ec);
            }
            // Remove equivalence classes with duplicates
            for (Set<T> ec : ecWithElement) {
                equivalenceClasses.remove(ec);
            }
            // Add the new equivalence class
            equivalenceClasses.add(newEc);
        }
    }

    @Override
    public String toString() {
        return equivalenceClasses.toString();
    }

}
