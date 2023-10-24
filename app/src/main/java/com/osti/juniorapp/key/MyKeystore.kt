package com.osti.juniorapp.key

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.db.tables.AngelTable
import com.osti.juniorapp.preferences.JuniorShredPreferences
import com.osti.juniorapp.utils.MyBase64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

class MyKeystore (context: Context,val angel:String?){

    companion object{
        const val alias = "JuniorAlias"
    }

    var activeKey:String? = getKey(context)

    fun setKey(key:String, context:Context){
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        val secretKey = keyGenerator.generateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv;
        val encryption = cipher.doFinal(key.toByteArray(Charsets.UTF_8))
        val tmp = MyBase64.encode(iv)
        JuniorShredPreferences.setSharedPref(tmp, "cipfher", context)
        //JuniorShredPreferences.setSharedPref(MyBase64.encode(encryption), "encryption", context)
        val tmpAngel = AngelTable(value = MyBase64.encode(encryption))
        JuniorApplication.myDatabaseController.insertAngel(tmpAngel)
        activeKey = tmpAngel.value
    }

   private fun getKey(context: Context) :String?{
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            val secretKeyEntry = keyStore
                .getEntry(alias, null) as KeyStore.SecretKeyEntry

            val secretKey = secretKeyEntry.secretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val tmp =
                MyBase64.decode(JuniorShredPreferences.getSharedPref("cipfher", context))
            val spec = GCMParameterSpec(128, tmp)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decodedData = cipher.doFinal(
                MyBase64.decode(angel)
            )

            return String(decodedData, Charsets.UTF_8)

        }
        catch (e: Exception) {
            val i = e
            return null
        }
    }
}