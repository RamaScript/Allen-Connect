plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.ramascript.allenconnect"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ramascript.allenconnect"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "4.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.MrNouri:DynamicSizes:1.0")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.firebase:firebase-storage")
    implementation ("com.squareup.picasso:picasso:2.8")

    implementation("com.github.marlonlom:timeago:4.0.3")

    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation ("com.google.android.material:material:1.9.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
}