package io.rebble.cobble.shared.js

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.api.RWS
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.domain.common.PebbleDevice
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.security.MessageDigest
import java.util.Locale

object JsTokenUtil: KoinComponent {
    private val lockerDao: LockerDao by inject()
    private const val ACCOUNT_TOKEN_SALT = "MMIxeUT[G9/U#(7V67O^EuADSw,{\$C;B}`>|-  lrQCs|t|k=P_!*LETm,RKc,BG*'"

    private fun md5Digest(input: String): String {
        val digest = MessageDigest.getInstance("md5")
        digest.update(input.toByteArray())
        val bytes = digest.digest()
        return bytes.joinToString(separator = "") { String.format("%02X", it) }.lowercase(Locale.US)
    }

    private suspend fun generateToken(uuid: Uuid, seed: String): String {
        val developerId = lockerDao.getEntryByUuid(uuid.toString())?.entry?.developerId
        val unhashed = buildString {
            append(seed)
            append(developerId ?: uuid.toString().uppercase(Locale.US))
            append(ACCOUNT_TOKEN_SALT)
        }
        return md5Digest(unhashed)
    }

    suspend fun getWatchToken(uuid: Uuid, device: PebbleDevice): String {
        val serial = device.metadata.value?.serial?.get() ?: throw IllegalArgumentException("Device has no serial")
        return generateToken(uuid, serial)
    }

    suspend fun getAccountToken(uuid: Uuid): String? {
        return RWS.authClient?.getCurrentAccount()?.uid?.toString()?.let { generateToken(uuid, it) }
    }
}