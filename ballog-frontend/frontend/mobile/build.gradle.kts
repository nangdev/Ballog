plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.spotless)
    id("kotlin-parcelize")

}

// AWS 자격 증명은 안드로이드 앱에서 직접 로드하도록 설정
// assets/aws.properties에서 로드됨

android {
    namespace = "com.ballog.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ballog.mobile"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 빈 AWS 키 설정 - 실제 키는 S3Utils에서 assets/aws.properties에서 로드
        buildConfigField("String", "AWS_ACCESS_KEY", "\"\"")
        buildConfigField("String", "AWS_SECRET_KEY", "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true  // BuildConfig 클래스 생성 활성화
    }

    // ✅ composeOptions는 생략 가능하나 명시하려면 최신 버전으로 맞춤
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.tv.material)
    val composeBom = platform("androidx.compose:compose-bom:2024.03.00")
    implementation(composeBom)
    androidTestImplementation(composeBom) // ✅ 추가

    // SamSung SDK
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.gson)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.compose.foundation:foundation:1.4.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // AWS S3
    implementation("com.amazonaws:aws-android-sdk-s3:2.71.0")
    implementation("com.amazonaws:aws-android-sdk-mobile-client:2.71.0")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4") // ✅ BOM 적용됨

    // Wearable
    implementation(libs.play.services.wearable.v1800)

    // Media3 (ExoPlayer 최신 버전)
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    
}

