package de.itemis.mosig.fluffy.tests.java;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeArchives;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "de.itemis.mosig.fluffy.tests.java", importOptions = {DoNotIncludeTests.class, DoNotIncludeArchives.class})
public class CyclicDependenciesTest {

    @ArchTest
    static final ArchRule PACKAGE_DEPENDENCIES_SHOULD_NOT_FORM_A_CYCLE =
        slices().matching("de.itemis.mosig.fluffy.tests.java.(**)").should().beFreeOfCycles();
}
