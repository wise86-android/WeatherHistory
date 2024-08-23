// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("jvm") version "1.8.10" // or kotlin("multiplatform") or any other kotlin plugin
    kotlin("plugin.serialization") version "1.8.10"

    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false


}