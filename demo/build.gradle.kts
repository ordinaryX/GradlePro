project.ext.set("mainApp", true)
plugins {
    id("com.wln.plugin.moduleRegister")
    id("org.jetbrains.kotlin.android")
}

val sdkVersion = 34
val minSdkVersion = 23

android {
    namespace = "com.wln.demo"
    compileSdk = sdkVersion

    defaultConfig {
        manifestPlaceholders["minSdkVersion"] = minSdkVersion
        manifestPlaceholders["targetSdkVersion"] = sdkVersion
        manifestPlaceholders["maxSdkVersion"] = sdkVersion
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
//    constraints {
//        implementation("androidx.lifecycle:lifecycle-runtime:2.6.1")
        implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
//    }
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}