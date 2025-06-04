import com.android.build.gradle.api.LibraryVariant

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    id("checkstyle")
}

android {
    namespace = "de.blinkt.openvpn"
    compileSdk = 35
    ndkVersion = "28.0.13004108"

    defaultConfig {
        minSdk = 21
        targetSdk = 35

        // Force OpenVPN 2 behavior
        buildConfigField("boolean", "openvpn3", "false")

        externalNativeBuild {
            cmake {
                // No additional args
            }
        }
    }

    buildTypes {
        getByName("debug") {
            matchingFallbacks += listOf("skeleton")
        }
        getByName("release") {
            matchingFallbacks += listOf("skeleton")
        }
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }

    externalNativeBuild {
        cmake {
            path = File("${projectDir}/main/src/main/cpp/CMakeLists.txt")
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "build/ovpnassets")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        enable += setOf("BackButton", "EasterEgg", "StopShip", "IconExpectedSize", "GradleDynamicVersion", "NewerVersionAvailable")
        checkOnly += setOf("ImpliedQuantity", "MissingQuantity")
        disable += setOf("MissingTranslation", "UnsafeNativeCodeLocation")
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    packaging {
        jniLibs.useLegacyPackaging = true
    }

    flavorDimensions += "mode" // Define the dimension (can be any name)

    productFlavors {
        create("skeleton") {
            dimension = "mode"
            // You can customize this flavor’s config here
        }
//        create("ovpn2") {
//            dimension = "mode"
//        }
    }
}



kotlin {
    jvmToolchain(17)
}

var swigcmd = "swig"
if (file("/opt/homebrew/bin/swig").exists())
    swigcmd = "/opt/homebrew/bin/swig"
else if (file("/usr/local/bin/swig").exists())
    swigcmd = "/usr/local/bin/swig"

fun registerGenTask(variantName: String, variantDirName: String): File {
    val baseDir = File(buildDir, "generated/source/ovpn3swig/${variantDirName}")
    val genDir = File(baseDir, "net/openvpn/ovpn3")

    tasks.register<Exec>("generateOpenVPN3Swig${variantName}") {
        doFirst {
            mkdir(genDir)
        }
        commandLine(
            listOf(
                swigcmd, "-outdir", genDir, "-outcurrentdir", "-c++", "-java", "-package", "net.openvpn.ovpn3",
                "-Isrc/main/cpp/openvpn3/client", "-Isrc/main/cpp/openvpn3/",
                "-DOPENVPN_PLATFORM_ANDROID",
                "-o", "${genDir}/ovpncli_wrap.cxx", "-oh", "${genDir}/ovpncli_wrap.h",
                "src/main/cpp/openvpn3/client/ovpncli.i"
            )
        )
        inputs.file("main/src/main/cpp/openvpn3/client/ovpncli.i")
        outputs.dir(genDir)
    }

    return baseDir
}

//android.libraryVariants.all(object : Action<LibraryVariant> {
//    override fun execute(variant: LibraryVariant) {
//        val sourceDir = registerGenTask(variant.name, variant.baseName.replace("-", "/"))
//        val task = tasks.named("generateOpenVPN3Swig${variant.name}").get()
//        variant.registerJavaGeneratingTask(task, sourceDir)
//    }
//})

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)

    implementation(libs.android.view.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.webkit)
    implementation(libs.kotlin)
    implementation(libs.mpandroidchart)
    implementation(libs.square.okhttp)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
}
