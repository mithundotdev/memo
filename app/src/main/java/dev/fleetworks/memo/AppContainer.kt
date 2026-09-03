package dev.fleetworks.memo

import dev.fleetworks.memo.chat.ChatRunner
import dev.fleetworks.memo.core.NoteRepository
import dev.fleetworks.memo.core.profile.ProfileStore

data class AppContainer(
    val repo: NoteRepository,
    val profiles: ProfileStore,
    val chatRunner: ChatRunner
)
