import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")

}

val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}
val midtransClientKey = localProperties.getProperty("MIDTRANS_CLIENT_KEY")

android {
    namespace = "com.example.project"
    compileSdk = 35

    buildFeatures{
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.project"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MIDTRANS_CLIENT_KEY", "\"$midtransClientKey\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.gridlayout)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.bcrypt)
    implementation(libs.jbcrypt)
    implementation(libs.okhttp)
    implementation (libs.github.glide)
    annotationProcessor (libs.compiler)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.kotlin.codegen)
    implementation(libs.retrofit2.retrofit)

    implementation ("com.midtrans:uikit:2.0.0-SANDBOX")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation("androidx.room:room-runtime:2.7.1")
    kapt("androidx.room:room-compiler:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
}