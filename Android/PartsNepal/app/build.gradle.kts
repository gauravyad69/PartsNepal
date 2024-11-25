plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.navigation.safeargs)
    id("com.google.devtools.ksp")

}

android {
    namespace = "np.com.parts"
    compileSdk = 34

    defaultConfig {
        applicationId = "np.com.parts"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

 implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation (libs.media3.common)
//    implementation (libs.navigation.fragment)
//    implementation (libs.navigation.ui)
    implementation(libs.smoothbottombar)
    implementation(libs.material)
    implementation(libs.coil)
    implementation(libs.whynot.imagecarousel)
    implementation(libs.facebook.shimmer)
    implementation(libs.airbnb.paris)



    implementation(libs.mikepenz.fastadapter)
    implementation(libs.mikepenz.fastadapter.binding)
    implementation(libs.mikepenz.fastadapter.scroll)
    implementation(libs.mikepenz.fastadapter.util)
    implementation(libs.androidx.security.crypto)

    
    // Your existing dependencies
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.core)
    implementation(libs.timber)

    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.work.runtime)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    implementation(libs.firebase.dataconnect)
    implementation(libs.androidx.swiperefreshlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.activity.ktx)
//configurations.all {
//    resolutionStrategy {
//        force("org.jetbrains:annotations:24.1.0")
//        force("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
//        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
//        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22")
//    }
//}

    constraints {
        implementation("org.jetbrains:annotations:24.1.0") {
            because("Resolve duplicate class conflicts")
        }
    }
    
    modules {
        module("com.intellij:annotations") {
            replacedBy("org.jetbrains:annotations", "Replaced to avoid duplicate classes")
        }
    }
}