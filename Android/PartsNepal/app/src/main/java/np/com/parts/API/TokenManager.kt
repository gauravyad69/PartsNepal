package np.com.parts.API

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
    }

    fun saveToken(token: String) {
        Timber.tag("TokenManager").d("Saving token: ${token.take(10)}...")
        sharedPreferences.edit()
            .putString(KEY_JWT_TOKEN, token)
            .commit()
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString(KEY_JWT_TOKEN, null)
        Timber.tag("TokenManager").d("Retrieved token: ${token?.take(10)}...")
        return token
    }

    fun clearToken() {
        Timber.tag("TokenManager").d("Clearing token")
        sharedPreferences.edit()
            .remove(KEY_JWT_TOKEN)
            .commit()
    }

    fun hasToken(): Boolean = getToken() != null
} 