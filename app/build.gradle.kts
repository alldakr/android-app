plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.mynfc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mynfc"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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

    // Compose와 KotlinOptions 제거
    buildFeatures {
        compose = false  // Compose 비활성화
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 필요 없는 Kotlin 및 Compose 라이브러리 제거
    implementation("androidx.core:core-ktx:1.9.0") // Android Core
    implementation("androidx.appcompat:appcompat:1.6.1") // AppCompat
    implementation("com.google.android.material:material:1.8.0") // Material Components
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // ConstraintLayout

    // 테스트 라이브러리
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Debug 및 Android Test 관련 라이브러리 제거
    // 필요 시 아래 라이브러리를 추가
    // debugImplementation "androidx.fragment:fragment-testing:1.5.7"
}

