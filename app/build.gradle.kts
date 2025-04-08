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
    
    // Add packaging options to handle duplicate files
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES", 
                "META-INF/LICENSE", 
                "META-INF/LICENSE.txt", 
                "META-INF/license.txt", 
                "META-INF/NOTICE", 
                "META-INF/NOTICE.txt", 
                "META-INF/notice.txt", 
                "META-INF/ASL2.0", 
                "META-INF/*.kotlin_module"
            )
        }
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

    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation ("com.google.android.material:material:1.9.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    
    // Camera permissions handling
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    
    // Permissions handling
    implementation("com.guolindev.permissionx:permissionx:1.7.1")


    // Add PDFBox for PDF text extraction
    implementation ("org.apache.pdfbox:pdfbox:2.0.27")
    implementation ("org.apache.pdfbox:fontbox:2.0.27")

    // Add Apache POI for DOCX, XLSX text extraction
    implementation ("org.apache.poi:poi:5.2.3")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")

    // Gemini AI API
    implementation ("com.google.ai.client.generativeai:generativeai:0.1.1")
}