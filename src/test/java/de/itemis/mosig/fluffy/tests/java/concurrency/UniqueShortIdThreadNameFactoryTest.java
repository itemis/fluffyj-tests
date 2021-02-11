package de.itemis.mosig.fluffy.tests.java.concurrency;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertFinal;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class UniqueShortIdThreadNameFactoryTest {

    private static final int ID_SEPARATOR_LENGTH = 1;
    private static final String ID_SEPARATOR = "-";
    private static final int CHARACTERS_PER_BYTE = 2;
    private static final int EXPECTED_UNIQUE_ID_COUNT = Short.MAX_VALUE;
    private static final String EXPECTED_PREFIX = "expectedPrefix";
    private static final int EXPECTED_ID_LENGTH_IN_CHARACTERS = Short.BYTES * CHARACTERS_PER_BYTE;

    private UniqueShortIdThreadNameFactory underTest;

    @BeforeEach
    public void setUp() {
        underTest = new UniqueShortIdThreadNameFactory(EXPECTED_PREFIX);
    }

    @Test
    public void is_final() {
        assertFinal(UniqueShortIdThreadNameFactory.class);
    }

    @Test
    public void generated_name_contains_prefix() {
        String result = generateName();
        assertThat(result).startsWith(EXPECTED_PREFIX);
    }

    @Test
    public void generated_name_has_expected_length() {
        String result = generateName();
        assertThat(result).hasSize(EXPECTED_PREFIX.length() + ID_SEPARATOR_LENGTH + EXPECTED_ID_LENGTH_IN_CHARACTERS);
    }

    @Test
    public void id_suffix_is_hex_encoded() {
        String id = extractIdSuffix(generateName());
        assertThat(id).matches("^[0-9a-f]+$");
    }

    @Test
    public void generated_names_are_unique() {
        Set<String> generatedNames = new HashSet<>();
        for (int i = 0; i < EXPECTED_UNIQUE_ID_COUNT; i++) {
            generatedNames.add(generateName());
        }

        assertThat(generatedNames).as("Encountered 'unique' id that has already been generated.").hasSize(EXPECTED_UNIQUE_ID_COUNT);
    }

    /*
     * #################
     *
     * Start helper code
     *
     * #################
     */

    private String extractIdSuffix(String generatedName) {
        int lastPosOfSeparator = generatedName.lastIndexOf(ID_SEPARATOR);
        return generatedName.substring(lastPosOfSeparator + ID_SEPARATOR_LENGTH);
    }

    private String generateName() {
        String result = underTest.generate();
        return result;
    }
}
