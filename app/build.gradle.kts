import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.medsdate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.medsdate"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("config") {
            // Configuration will be loaded from gradle.properties
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("config")
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
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Configure Kotlin to use JDK 17
kotlin {
    jvmToolchain(17)
}

// Load signing config from gradle.properties
val properties = Properties()
val propFile = file("../gradle.properties")
if (propFile.canRead()) {
    propFile.inputStream().use { properties.load(it) }

    if (properties.containsKey("STORE_FILE") &&
        properties.containsKey("STORE_PASSWORD") &&
        properties.containsKey("KEY_ALIAS") &&
        properties.containsKey("KEY_PASSWORD")
    ) {
        android.signingConfigs.getByName("config") {
            storeFile = file(properties.getProperty("STORE_FILE"))
            storePassword = properties.getProperty("STORE_PASSWORD")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASSWORD")
        }
    } else {
        println("gradle.properties found but some entries are missing")
        android.buildTypes.getByName("release").signingConfig = null
    }
} else {
    println("gradle.properties not found")
    android.buildTypes.getByName("release").signingConfig = null
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM (Bill of Materials)
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // WorkManager for notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Coil for image loading (Compose-compatible)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Permissions (Accompanist)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Billing
    implementation("com.android.billingclient:billing-ktx:6.1.0")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Timber for logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Koin for Dependency Injection
    val koinVersion = "3.5.3"
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

apply(plugin = "com.google.gms.google-services")
