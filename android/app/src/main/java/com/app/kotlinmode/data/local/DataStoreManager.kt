package com.app.kotlinmode.data.local

/**
 * DataStoreManager is no longer used.
 * JWT storage has been consolidated into:
 *   com.app.kotlinmode.repository.SessionManager
 *
 * This file is kept as a placeholder to avoid breaking old import references.
 * SessionManager is the single source of truth for DataStore in this project.
 */
@Deprecated("Use com.app.kotlinmode.repository.SessionManager instead")
object DataStoreManager
