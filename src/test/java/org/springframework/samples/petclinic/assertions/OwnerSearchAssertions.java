package org.springframework.samples.petclinic.assertions;

import org.assertj.core.api.Assertions;
import org.springframework.samples.petclinic.model.Owner;

import java.util.Collection;

public final class OwnerSearchAssertions {

    private OwnerSearchAssertions() {
    }

    public static void assertOwnersLastNameContainsIgnoreCase(Collection<Owner> owners, String substring) {
        Assertions.assertThat(owners).isNotNull();
        String needle = substring == null ? "" : substring.toLowerCase();

        Assertions.assertThat(owners)
            .allSatisfy(owner -> {
                Assertions.assertThat(owner).isNotNull();
                Assertions.assertThat(owner.getLastName()).isNotNull();
                Assertions.assertThat(owner.getLastName().toLowerCase()).contains(needle);
            });
    }
}
