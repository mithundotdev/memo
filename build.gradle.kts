plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force(
                "io.ktor:ktor-client-core:2.3.12",
                "io.ktor:ktor-client-okhttp:2.3.12",
                "io.ktor:ktor-client-logging:2.3.12",
                "io.ktor:ktor-client-auth:2.3.12",
                "io.ktor:ktor-client-content-negotiation:2.3.12",
                "io.ktor:ktor-http:2.3.12",
                "io.ktor:ktor-utils:2.3.12",
                "io.ktor:ktor-io:2.3.12",
                "io.ktor:ktor-events:2.3.12",
                "io.ktor:ktor-serialization:2.3.12",
                "io.ktor:ktor-websockets:2.3.12",
                "io.ktor:ktor-websocket-serialization:2.3.12"
            )
        }
    }
}
