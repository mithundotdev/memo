# Memo

Offline first Markdown notes plus BYOK chat.

## What

Notes list with search and tag chips. Note editor for title plus body. Chat with tool access to notes. Settings for provider baseUrl key and model.

## Run steps

Set ANDROID_HOME to the Android SDK path. Use Java 17 toolchain. Run `./gradlew :app:assembleDebug`. Install `app/build/outputs/apk/debug/app-debug.apk`. Run unit tests with `./gradlew :core:testDebugUnitTest :chat:testDebugUnitTest`.

## Notes

Tags derive at read by regex. Backlinks derive by FTS query on title. Delete from chat needs explicit confirm.
