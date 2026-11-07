group = "app.modprod"

patches {
    about {
        name = "Signals Auto Trading Patches"
        description = "Morphe patches for Signals (com.zyncas.signals): Auto Trading tab instead of Launchpad."
        source = "git@github.com:MOD-PROD/signal-patch.git"
        author = "ModProd"
        contact = "https://github.com/MOD-PROD/signal-patch/issues"
        website = "https://github.com/MOD-PROD/signal-patch"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    implementation(libs.morphe.patches.library)
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("app.modprod.patches.util.PatchListGeneratorKt")
    }

    publish {
        dependsOn("generatePatchesList")
    }
}
