package dev.fleetworks.memo

import android.app.Application
import dev.fleetworks.memo.chat.ChatRunner
import dev.fleetworks.memo.core.db.MemoDatabase
import dev.fleetworks.memo.core.db.RoomNoteRepository
import dev.fleetworks.memo.core.profile.ProfileStore

class MemoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val db = MemoDatabase.create(this)
        val repo = RoomNoteRepository(db)
        val profiles = ProfileStore(this)
        container = AppContainer(repo, profiles, ChatRunner(repo))
    }
}
