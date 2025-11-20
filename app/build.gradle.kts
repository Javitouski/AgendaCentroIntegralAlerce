plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "prog.android.centroalr"
    compileSdk = 36

    defaultConfig {
        applicationId = "prog.android.centroalr"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // =======================================
    // 🔥 F I R E B A S E   (CORRECTO)
    // =======================================

    // Usamos SOLO UN BOM (válido y estable)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Core
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Debug App Check
    implementation("com.google.firebase:firebase-appcheck-debug:17.1.2")

    // =======================================
    // 🔧 ANDROIDX + UI
    // =======================================

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // ❌ ELIMINADO: implementation(libs.firebase.firestore)
    // (duplicaba Firestore y generaba conflicto)

    // =======================================
    // 🧪 TESTING
    // =======================================

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
