package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.struct.Partition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartitionTest {

    @Test
    @DisplayName("Adding two elements")
    public void addWithTwoArgs_shouldAddElemToExistingEC_ifOtherElemPresent() {
        Partition<Integer> partition = new Partition<>();
        partition.add(1);
        assertEquals(1, partition.equivalenceClasses().size());
        partition.add(1, 2);
        assertEquals(1, partition.equivalenceClasses().size());
    }

    @Test
    @DisplayName("Merging")
    public void shouldMergeEC_ifHasECWithDuplicateElems() {
        Partition<Integer> partition = new Partition<>();
        partition.add(1, 2);
        assertEquals(1, partition.equivalenceClasses().size());
        partition.add(3, 4);
        assertEquals(2, partition.equivalenceClasses().size());
        partition.add(2, 3);
        assertEquals(1, partition.equivalenceClasses().size());
    }

}
