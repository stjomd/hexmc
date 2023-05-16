package at.ac.tuwien.student.e11843614.example;

import at.ac.tuwien.student.e11843614.struct.Partition;

import java.util.HashSet;
import java.util.Set;

public abstract class PartitionChecks {

    public static <T> boolean isRefinement(Partition<T> thi, Partition<T> p) {
        for (Set<T> ecThis : thi.equivalenceClasses()) {
            boolean found = false;
            for (Set<T> ecP : p.equivalenceClasses()) {
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

    public static <T> boolean isBinaryRefinement(Partition<T> thi, Partition<T> p) {
        if (!isRefinement(thi, p)) {
            return false;
        }
        // For all pEC in p ...
        for (Set<T> pEC : p.equivalenceClasses()) {
            // In this, there must be [ec_1, ec_2]  s.t.  pEC is a subset of union([ec_1, ec_2]).
            // Iterate over pairs of (ec1,ec2), check if union is superset.
            boolean fulfils = false;
            for (Set<T> ec1 : thi.equivalenceClasses()) {
                for (Set<T> ec2 : thi.equivalenceClasses()) {
                    if (ec1 != ec2) {
                        Set<T> union = new HashSet<>(ec1);
                        union.addAll(ec2);
                        if (union.containsAll(pEC)) {
                            fulfils = true;
                            break;
                        }
                    }
                }
                if (fulfils)
                    break;
            }
            // If no pair was found, return false, otherwise check next pEC.
            if (!fulfils)
                return false;
        }
        return true;
    }

    public static <T> boolean isTernaryRefinement(Partition<T> thi, Partition<T> p) {
        if (!isRefinement(thi, p)) {
            return false;
        }
        // For all pEC in p ...
        for (Set<T> pEC : p.equivalenceClasses()) {
            // In this, there must be [ec_1, ec_2, ec_3]  s.t.  pEC is a subset of union([ec_1, ec_2, ec_3]).
            // Iterate over pairs of (ec1,ec2,ec3), check if union is superset.
            boolean fulfils = false;
            for (Set<T> ec1 : thi.equivalenceClasses()) {
                for (Set<T> ec2 : thi.equivalenceClasses()) {
                    for (Set<T> ec3: thi.equivalenceClasses()) {
                        if (ec1 != ec2 && ec2 != ec3 && ec3 != ec1) {
                            Set<T> union = new HashSet<>(ec1);
                            union.addAll(ec2);
                            union.addAll(ec3);
                            if (union.containsAll(pEC)) {
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
            // If no pair was found, return false, otherwise check next pEC.
            if (!fulfils)
                return false;
        }
        return true;
    }

}
