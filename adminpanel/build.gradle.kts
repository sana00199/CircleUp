plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.sana.adminpanel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sana.adminpanel"
        minSdk = 27
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.play.services.auth)
    implementation (libs.circleimageview)
    implementation (libs.material) // or the latest version

    implementation (libs.glide)
    annotationProcessor (libs.compiler)

    implementation (libs.firebase.database.v2000)
    implementation (libs.firebase.auth.v2100)

    implementation (libs.sdp.android)

    implementation (libs.picasso)



//    implementation ("com.google.firebase:firebase-auth:23.2.0")
//    implementation ("com.google.firebase:firebase-database:20.3.0")
//    implementation ("com.google.firebase:firebase-storage:20.3.0")


    // Firebase BoM (Bill of Materials)
    implementation (platform(libs.firebase.bom))

    // Firebase Authentication
    implementation (libs.google.firebase.auth)

    // Firebase Analytics (recommended)
    implementation (libs.firebase.analytics)

}