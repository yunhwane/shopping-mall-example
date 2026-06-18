package com.example.shoppingmall

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModularityTests {

    private val modules = ApplicationModules.of(ShoppingMallApplication::class.java)

    // Fails the build if any module reaches into another's internals.
    @Test
    fun verifiesModuleBoundaries() {
        modules.verify()
    }

    // ponytail: writes a PlantUML/C4 view of the modules under build/spring-modulith-docs.
    // Handy when explaining the architecture; delete if it ever gets noisy.
    @Test
    fun writesDocumentation() {
        org.springframework.modulith.docs.Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
    }
}
