import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val tmdbReadAccessToken = providers.gradleProperty("TMDB_READ_ACCESS_TOKEN")
    .orElse("")
    .map { token ->
        token.trim()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
    }

fun readFirebaseOptions(flavor: String, packageName: String): Map<String, String> {
    val files = listOf("src/$flavor/google-services.json", "google-services.json")
    for (path in files) {
        val file = layout.projectDirectory.file(path)
        val content = providers.fileContents(file).asText.orNull ?: continue
        val config = groovy.json.JsonSlurper().parseText(content) as Map<*, *>
        val project = config["project_info"] as Map<*, *>
        for (entry in config["client"] as List<*>) {
            val client = entry as Map<*, *>
            val info = client["client_info"] as Map<*, *>
            val androidInfo = info["android_client_info"] as? Map<*, *>
            if (androidInfo?.get("package_name") != packageName) continue

            val apiKey = (client["api_key"] as List<*>).first() as Map<*, *>
            return mapOf(
                "google_app_id" to (info["mobilesdk_app_id"] as String),
                "google_api_key" to (apiKey["current_key"] as String),
                "project_id" to (project["project_id"] as String),
                "gcm_defaultSenderId" to (project["project_number"] as String),
            )
        }
    }
    return emptyMap()
}

android {
    namespace = "id.application.padsmovie"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "id.application.padsmovie"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TMDB_READ_ACCESS_TOKEN", "\"${tmdbReadAccessToken.get()}\"")
    }

    flavorDimensions += "environment"
    productFlavors {
        create("development") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }
        create("production") {
            dimension = "environment"
        }
    }

    productFlavors.configureEach {
        val packageName = "id.application.padsmovie" + applicationIdSuffix.orEmpty()
        readFirebaseOptions(name, packageName).forEach { (key, value) ->
            resValue("string", key, value)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
