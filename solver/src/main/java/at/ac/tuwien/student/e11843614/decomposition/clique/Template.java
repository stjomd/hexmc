package at.ac.tuwien.student.e11843614.decomposition.clique;

import at.ac.tuwien.student.e11843614.struct.Partition;

public class Template {

    private final Partition<Integer> components = new Partition<>();
    private final Partition<Integer> groups = new Partition<>();

    public Partition<Integer> getComponents() {
        return components;
    }

    public Partition<Integer> getGroups() {
        return groups;
    }

}
