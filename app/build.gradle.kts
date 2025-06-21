plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.sana.circleup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sana.circleup"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
        // You can also add other exclusions if needed
        // pickFirst 'META-INF/DEPENDENCIES' // Alternatively, use this if you want to pick the first occurrence
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
//    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.recyclerview)
//    implementation(libs.volley)
    implementation(libs.scenecore)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.play.services.auth)
    implementation(libs.circleimageview)
    annotationProcessor(libs.compiler)
    implementation(libs.sdp.android)
//    implementation(libs.picasso)

    // Firebase BoM (Bill of Materials)
    implementation(platform(libs.firebase.bom))

    // Firebase libraries (no version needed when using BoM)
    implementation(libs.firebase.auth)
//    implementation(libs.firebase.analytics)
    implementation(libs.firebaseui.firebase.ui.database)
    implementation(libs.firebase.messaging) // Use BoM for versioning

//    implementation (libs.firebase.messaging.v2312)

    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)


// OR if WorkManager specifically needs it, sometimes this one is suggested:
// implementation("androidx.work:work-listenablefuture:2.9.0") // Use your work-runtime version if different

    // Google API Client Libraries
    implementation(libs.google.api.client)
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.google.auth.library.oauth2.http)


    implementation (libs.work.runtime)

    // Retrofit and OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.converter.scalars)
    implementation (libs.onesignal.v5130)

    implementation (libs.glide.v4151) // Check for the latest version
    annotationProcessor (libs.compiler) // Match the version

    implementation ("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0") // <-- Yeh line add karain (latest version check kar sakti hain)

    implementation (libs.gson)

    implementation (libs.bcprov.jdk15on) // For standard algorithms
    implementation (libs.bcpkix.jdk15on) // For key formats, etc.

    implementation("androidx.security:security-crypto:1.1.0-alpha07")


}