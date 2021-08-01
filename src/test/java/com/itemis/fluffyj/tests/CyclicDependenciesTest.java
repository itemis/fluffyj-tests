package com.itemis.fluffyj.tests;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeArchives;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.itemis.fluffyj.tests", importOptions = {DoNotIncludeTests.class, DoNotIncludeArchives.class})
public class CyclicDependenciesTest {

    @ArchTest
    static final ArchRule PACKAGE_DEPENDENCIES_SHOULD_NOT_FORM_A_CYCLE =
        slices().matching("com.itemis.fluffyj.tests.(**)").should().beFreeOfCycles();
}
