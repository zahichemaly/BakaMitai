import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10"
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zc.bakamitai"
        minSdk = 23
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
    namespace = "com.zc.bakamitai"
}

dependencies {
    val retrofit = "3.0.0"
    val koin = "4.1.1"
    val room = "2.8.4"
    val glide = "5.0.5"

    // AndroidX
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.6")
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")

    //region New dependencies after re-write
    val ktorVersion = "3.4.0" // Use latest version
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:${ktorVersion}")
    implementation("io.ktor:ktor-client-logging:${ktorVersion}")

    implementation("io.insert-koin:koin-androidx-compose:${koin}")
    implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    //endregion

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation("androidx.compose.ui:ui-viewbinding:1.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.6")


    // Networking & Serialization
    implementation("com.squareup.retrofit2:retrofit:$retrofit")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofit")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.jsoup:jsoup:1.21.2")

    // DI
    implementation("io.insert-koin:koin-core:$koin")
    implementation("io.insert-koin:koin-android:$koin")
    implementation("io.insert-koin:koin-android-compat:$koin")
    implementation("io.insert-koin:koin-androidx-workmanager:$koin")
    implementation("io.insert-koin:koin-androidx-navigation:$koin")

    // Database
    implementation("androidx.room:room-runtime:$room")
    ksp("androidx.room:room-compiler:$room")

    // Image
    implementation("com.github.bumptech.glide:glide:$glide")
    ksp("com.github.bumptech.glide:compiler:$glide")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    //implementation("com.google.firebase:firebase-analytics-ktx")

    // Misc
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.ms-square:expandableTextView:0.1.4")
    implementation("com.google.android.material:material:1.13.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
