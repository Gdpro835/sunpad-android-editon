plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sunpad.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sunpad.android"
        // Android 8.0+: OpenGL ES 3.0 + OpenSL ES baseline (оптимизировано
        // для слабых устройств, Vulkan полностью отключён).
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-native-android"

        // Pin the same NDK that scripts/android-build-core.sh uses for the
        // core; AGP's default NDK (26.1, clang 17) does not accept
        // -std=c++23.
        ndkVersion = "26.3.11579264"

        ndk {
            // Нативная ARM64 сборка: код Super Mario Sunshine заранее
            // перекомпилирован в ARM64, без JIT-эмуляции PowerPC и без
            // полного эмулятора Dolphin. Поддерживаются только 64-битные
            // устройства (все современные Android телефоны 2017+).
            abiFilters += listOf("arm64-v8a")
            // Отладочные символы из нативных библиотек вырезаем для
            // уменьшения размера APK и потребления памяти.
            debugSymbolLevel = "NONE"
        }

        externalNativeBuild {
            cmake {
                // No -std flag here: app/src/main/cpp/CMakeLists.txt sets
                // CMAKE_CXX_STANDARD 23 and CMake picks the right spelling
                // for the compiler (c++2b on NDK clang 17, c++23 on 18+).
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_PACKAGE_NATIVE_LIBS_STRIP_MODE=all",
                    "-DCMAKE_BUILD_TYPE=MinSizeRel"
                )
                // Оптимизация под размер кода для нативных библиотек
                cppFlags += listOf("-Os", "-ffunction-sections", "-fdata-sections")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        jniLibs {
            // Вырезаем отладочные символы из всех .so для уменьшения
            // размера APK (экономия ~30-50 МБ).
            keepDebugSymbols += listOf()
            useLegacyPackaging = false
        }
    }

    buildTypes {
        debug {
            isJniDebuggable = true
        }
        release {
            isMinifyEnabled = false
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
    testImplementation("junit:junit:4.13.2")
}
