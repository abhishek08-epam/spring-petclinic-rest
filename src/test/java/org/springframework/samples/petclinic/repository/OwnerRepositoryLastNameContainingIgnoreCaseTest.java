package org.springframework.samples.petclinic.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.samples.petclinic.model.Owner;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OwnerRepositoryLastNameContainingIgnoreCaseTest {

    @Autowired
    private OwnerRepository ownerRepository;

    private static List<String> sortedLastNames(Collection<Owner> owners) {
        return owners.stream()
            .map(Owner::getLastName)
            .sorted(Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    @Test
    @DisplayName("findByLastNameContainingIgnoreCase: returns owners whose lastName contains term regardless of case")
    void findByLastNameContainingIgnoreCase_containsAndIgnoreCase() {
        // Based on standard PetClinic sample data: 'Franklin' should match 'lin' and 'LIN'
        Collection<Owner> lower = ownerRepository.findByLastNameContainingIgnoreCase("lin");
        Collection<Owner> upper = ownerRepository.findByLastNameContainingIgnoreCase("LIN");

        assertThat(lower).isNotNull().isNotEmpty();
        assertThat(upper).isNotNull().isNotEmpty();

        assertThat(sortedLastNames(lower)).isEqualTo(sortedLastNames(upper));

        assertThat(lower.stream().map(Owner::getLastName))
            .allSatisfy(ln -> assertThat(ln.toLowerCase()).contains("lin"));
    }

    @Test
    @DisplayName("findByLastNameContainingIgnoreCase: when no matches -> returns empty (not null)")
    void findByLastNameContainingIgnoreCase_noMatches_returnsEmptyNotNull() {
        Collection<Owner> result = ownerRepository.findByLastNameContainingIgnoreCase("___definitely_no_match___");

        assertThat(result).isNotNull().isEmpty();
    }
}
