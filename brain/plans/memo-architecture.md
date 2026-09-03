# Memo v1 plan

## Goal

Offline first Markdown notes plus BYOK chat.

## Modules

Core owns Room plus FTS5 plus DataStore profile plus NoteRepository impl. Core has no project deps. Chat owns prompt build plus tool loop plus OpenAI wrapper. Chat depends on core only. App owns Compose Material3 UI plus ViewModels plus Nav plus manual AppContainer DI. App depends on chat and core.

## Data

Note has id UUID String plus title plus body Markdown plus createdAt plus updatedAt. Tags derive at read by regex. Tags are never stored. Backlinks derive by FTS query on title. Backlinks are never side written.

ChatMessage has sealed Role plus text plus toolCalls plus turnId. ProviderProfile has baseUrl plus apiKey plus model. Profile store exposes observeProfile as Flow of nullable Profile. Single DataStore row.

ToolCall has name enum plus typed args plus result. Tool names are search_notes plus get_note plus create_note plus update_note plus delete_note. ToolRegistry is a map from ToolName to suspend handler. No switch on name.

ChatRunner runTurn takes history plus profile plus confirmDelete. It returns Flow of ChatEvent. Max turns is 5. Delete runs only with an exact id from search or get_note. Delete runs only after confirmDelete resolves true at the app composition root.

## UI

Bottom nav has Notes plus Chat plus Settings. Notes list has search plus tag filter chips. Note detail has title plus body editor. Settings sheet has baseUrl plus key plus model plus Test connection button. Chat shows messages plus one line tool activity. Delete from chat raises a confirm dialog.

## FTS risk

Unicode61 strips brackets. Normalize open and close brackets to a sentinel token on FTS insert. Apply the same transform to search input. Fall back to LIKE query when FTS returns nothing.

## Tests

JVM only. Tag regex tests. Sentinel transform tests. ToolRegistry test against a fake NoteRepository with runTest. No Robolectric. No androidTest.
