package com.towerofdarkness.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.towerofdarkness.app.domain.cards.CardCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("tower_meta")

class MetaStore(private val context: Context) {
    private val KEY_TUTORIAL = booleanPreferencesKey("tutorial_seen")
    private val KEY_REMNANTS = intPreferencesKey("remnants_bank")
    private val KEY_UNLOCKED = stringSetPreferencesKey("unlocked_cards")
    private val KEY_META_HP = intPreferencesKey("meta_hp_bonus")

    val tutorialSeen: Flow<Boolean> = context.dataStore.data.map { it[KEY_TUTORIAL] ?: false }
    val remnantsBank: Flow<Int> = context.dataStore.data.map { it[KEY_REMNANTS] ?: 0 }
    val unlockedCards: Flow<Set<String>> = context.dataStore.data.map {
        it[KEY_UNLOCKED] ?: CardCatalog.starterUnlockedIds()
    }
    val metaHpBonus: Flow<Int> = context.dataStore.data.map { it[KEY_META_HP] ?: 0 }

    suspend fun setTutorialSeen(seen: Boolean = true) {
        context.dataStore.edit { it[KEY_TUTORIAL] = seen }
    }

    suspend fun addRemnants(amount: Int) {
        context.dataStore.edit {
            val cur = it[KEY_REMNANTS] ?: 0
            it[KEY_REMNANTS] = (cur + amount).coerceAtLeast(0)
        }
    }

    suspend fun spendRemnants(amount: Int): Boolean {
        var ok = false
        context.dataStore.edit {
            val cur = it[KEY_REMNANTS] ?: 0
            if (cur >= amount) {
                it[KEY_REMNANTS] = cur - amount
                ok = true
            }
        }
        return ok
    }

    suspend fun unlockCard(id: String) {
        context.dataStore.edit {
            val cur = (it[KEY_UNLOCKED] ?: CardCatalog.starterUnlockedIds()).toMutableSet()
            cur += id
            it[KEY_UNLOCKED] = cur
        }
    }

    suspend fun setMetaHpBonus(bonus: Int) {
        context.dataStore.edit { it[KEY_META_HP] = bonus }
    }
}
