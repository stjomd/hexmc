package at.ac.tuwien.student.e11843614.misc;

import at.ac.tuwien.student.e11843614.struct.Partition;

import java.util.HashSet;
import java.util.Set;

public abstract class PartitionChecks {

    public static <T> boolean isRefinement(Partition<T> p, Partition<T> q) {
        for (Set<T> pEC : p.equivalenceClasses()) {
            boolean found = false;
            for (Set<T> qEC : q.equivalenceClasses()) {
                if (qEC.containsAll(pEC)) {
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

    public static <T> boolean isBinaryRefinement(Partition<T> p, Partition<T> q) {
        if (!isRefinement(p, q)) {
            return false;
        }
        // For all qEC in q ...
        for (Set<T> qEC : q.equivalenceClasses()) {
            // In p, there must be [ec_1, ec_2]  s.t.  qEC is a subset of union([ec_1, ec_2]).
            // Iterate over pairs of (ec1,ec2), check if union is superset.
            boolean fulfils = false;
            for (Set<T> ec1 : p.equivalenceClasses()) {
                for (Set<T> ec2 : p.equivalenceClasses()) {
                    if (ec1 != ec2) {
                        Set<T> union = new HashSet<>(ec1);
                        union.addAll(ec2);
                        if (union.containsAll(qEC)) {
                            fulfils = true;
                            break;
                        }
                    }
                }
                if (fulfils)
                    break;
            }
            // If no pair was found, return false, otherwise check next qEC.
            if (!fulfils)
                return false;
        }
        return true;
    }

    public static <T> boolean isTernaryRefinement(Partition<T> p, Partition<T> q) {
        if (!isRefinement(p, q)) {
            return false;
        }
        // For all qEC in q ...
        for (Set<T> qEC : q.equivalenceClasses()) {
            // In p, there must be [ec_1, ec_2, ec_3]  s.t.  qEC is a subset of union([ec_1, ec_2, ec_3]).
            // Iterate over pairs of (ec1,ec2,ec3), check if union is superset.
            boolean fulfils = false;
            for (Set<T> ec1 : p.equivalenceClasses()) {
                for (Set<T> ec2 : p.equivalenceClasses()) {
                    for (Set<T> ec3: p.equivalenceClasses()) {
                        if (ec1 != ec2 && ec2 != ec3 && ec3 != ec1) {
                            Set<T> union = new HashSet<>(ec1);
                            union.addAll(ec2);
                            union.addAll(ec3);
                            if (union.containsAll(qEC)) {
                                fulfils = true;
                                break;
                            }
                        }
                    }
                    if (fulfils)
                        break;
                }
                if (fulfils)
                    break;
            }
            // If no pair was found, return false, otherwise check next qEC.
            if (!fulfils)
                return false;
        }
        return true;
    }

}
