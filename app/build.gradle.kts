plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.gms.google-services'
}
android {
    namespace 'com.haraj.alshafa'
    compileSdk 34
    defaultConfig {
        applicationId "com.haraj.alshafa"
        minSdk 24
        targetSdk 34
        versionCode 8
        versionName "8.0"
    }
    buildTypes {
        release {
            minifyEnabled false
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = '17'
    }
}
dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'
}
