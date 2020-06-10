package de.silpion.olmpoc

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.matrix.olm.OlmAccount
import org.matrix.olm.OlmException
import org.matrix.olm.OlmMessage
import org.matrix.olm.OlmSession

class MainActivity : AppCompatActivity() {

    init {
        System.loadLibrary("olm")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test01AliceToBob()
    }

    private fun test01AliceToBob() {
        val ONE_TIME_KEYS_NUMBER = 5
        var bobIdentityKey: String? = null
        var bobOneTimeKey: String? = null
        var bobAccount: OlmAccount? = null
        var aliceAccount: OlmAccount? = null

        // ALICE & BOB ACCOUNTS CREATION
        try {
            aliceAccount = OlmAccount()
            bobAccount = OlmAccount()
        } catch (e: OlmException) {
            Log.d("OLM", "exception #1: " + e.localizedMessage)
        }

        //Log.d("OLM", "bob: " + bobAccount.getOlmAccountId() + "" + aliceAccount.getOlmAccountId())

        // get bob identity key
        var bobIdentityKeys: Map<String?, String?>? = null
        try {
            bobIdentityKeys = bobAccount?.identityKeys()
        } catch (e: Exception) {
            Log.d("OLM", "exception #2: " + e.localizedMessage)
        }

        bobIdentityKey = getIdentityKey(bobIdentityKeys!!)
        //assertTrue(null != bobIdentityKey)

        // get bob one time keys
        try {
            bobAccount?.generateOneTimeKeys(ONE_TIME_KEYS_NUMBER)
        } catch (e: Exception) {
            Log.d("OLM", "exception #3: " + e.localizedMessage)
        }
        var bobOneTimeKeys: Map<String?, Map<String?, String?>?>? = null
        try {
            bobOneTimeKeys = bobAccount?.oneTimeKeys()
        } catch (e: Exception) {
            Log.d("OLM", "exception #4: " + e.localizedMessage)
        }

        bobOneTimeKey = getOneTimeKey(bobOneTimeKeys, 1)
        //assertNotNull(bobOneTimeKey)

        // CREATE ALICE SESSION
        var aliceSession: OlmSession? = null
        try {
            aliceSession = OlmSession()
        } catch (e: OlmException) {
            Log.d("OLM", "exception #5: " + e.localizedMessage)
        }

        // CREATE ALICE OUTBOUND SESSION and encrypt message to bob
        try {
            aliceSession?.initOutboundSession(aliceAccount, bobIdentityKey, bobOneTimeKey)
        } catch (e: Exception) {
            Log.d("OLM", "exception #6: " + e.localizedMessage)
        }
        val clearMsg = "Heloo bob , this is alice!"
        var encryptedMsgToBob: OlmMessage? = null
        try {
            encryptedMsgToBob = aliceSession?.encryptMessage(clearMsg)
        } catch (e: Exception) {
            Log.d("OLM", "exception #7: " + e.localizedMessage)
        }

        Log.d("OLM", "## test01AliceToBob(): encryptedMsg=" + encryptedMsgToBob?.mCipherText)

        // CREATE BOB INBOUND SESSION and decrypt message from alice
        var bobSession: OlmSession? = null
        try {
            bobSession = OlmSession()
        } catch (e: OlmException) {
            Log.d("OLM", "exception #8: " + e.localizedMessage)
        }

        try {
            bobSession?.initInboundSession(bobAccount, encryptedMsgToBob?.mCipherText)
        } catch (e: Exception) {
            Log.d("OLM", "exception #9: " + e.localizedMessage)
        }
        var decryptedMsg: String? = null
        try {
            decryptedMsg = bobSession?.decryptMessage(encryptedMsgToBob)
        } catch (e: Exception) {
            Log.d("OLM", "exception #10: " + e.localizedMessage)
        }

        Log.d("OLM", "MESSAGE EQUAL: " + (clearMsg == decryptedMsg))

        // clean objects..
        try {
            bobAccount?.removeOneTimeKeys(bobSession)
        } catch (e: Exception) {
            Log.d("OLM", "exception #11: " + e.localizedMessage)
        }

        // release accounts
        bobAccount?.releaseAccount()
        aliceAccount?.releaseAccount()

        // release sessions
        bobSession?.releaseSession()
        aliceSession?.releaseSession()
    }

    /**
     * Return the identity key [OlmAccount.JSON_KEY_IDENTITY_KEY] from the JSON object.
     * @param aIdentityKeysMap result of [OlmAccount.identityKeys]
     * @return identity key string if operation succeed, null otherwise
     */
    fun getIdentityKey(aIdentityKeysMap: Map<String?, String?>): String? {
        var idKey: String? = null
        try {
            idKey = aIdentityKeysMap[OlmAccount.JSON_KEY_IDENTITY_KEY]
        } catch (e: Exception) {
            Log.d("OLMKeyHelper", "exception: " + e.localizedMessage)
        }
        return idKey
    }

    /**
     * Return the fingerprint key [OlmAccount.JSON_KEY_FINGER_PRINT_KEY] from the JSON object.
     * @param aIdentityKeysMap result of [OlmAccount.identityKeys]
     * @return fingerprint key string if operation succeed, null otherwise
     */
    fun getFingerprintKey(aIdentityKeysMap: Map<String?, String?>): String? {
        var fingerprintKey: String? = null
        try {
            fingerprintKey = aIdentityKeysMap[OlmAccount.JSON_KEY_FINGER_PRINT_KEY]
        } catch (e: Exception) {
            Log.d("OLMKeyHelper", "exception: " + e.localizedMessage)
        }
        return fingerprintKey
    }

    /**
     * Return the first one time key from the JSON object.
     * @param aIdentityKeysMap result of [OlmAccount.oneTimeKeys]
     * @param aKeyPosition the position of the key to be retrieved
     * @return one time key string if operation succeed, null otherwise
     */
    fun getOneTimeKey(
        aIdentityKeysMap: Map<String?, Map<String?, String?>?>?,
        aKeyPosition: Int
    ): String? {
        var firstOneTimeKey: String? = null
        try {
            val generatedKeys = aIdentityKeysMap!![OlmAccount.JSON_KEY_ONE_TIME_KEY]!!
            firstOneTimeKey = ArrayList(generatedKeys.values)[aKeyPosition - 1]
        } catch (e: Exception) {
            Log.d("OLMKeyHelper", "exception: " + e.localizedMessage)
        }
        return firstOneTimeKey
    }
}

