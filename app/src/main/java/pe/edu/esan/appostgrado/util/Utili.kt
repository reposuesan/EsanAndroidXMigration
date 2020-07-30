package pe.edu.esan.appostgrado.util

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Utili {

    private val characterEncoding = "UTF-8"
    private val cipherTransformation = "AES/CBC/PKCS5Padding"
    private val aesEncryptionAlgorithm = "AES"

    fun jsObjectEncrypted(json : JSONObject, context: Context) : JSONObject? {
        try {
            val encriptado = encrypt(json.toString(), context.resources.getString(R.string.KeyEncripter)).replace("\n","")
            val jsObject = JSONObject()
            jsObject.put("request", encriptado)

            return jsObject
        } catch (ue: UnsupportedEncodingException) { }
        catch (ike: InvalidKeyException) { }
        catch (nae: NoSuchAlgorithmException) { }
        catch (iape: InvalidAlgorithmParameterException) { }
        catch (ibe: IllegalBlockSizeException) { }
        catch (bpe: BadPaddingException) { }
        catch (nspe: NoSuchPaddingException) { }
        catch (jex: JSONException) { }

        return null
    }

    fun jsArrayEncrypted(jarray : JSONArray, context: Context) : JSONObject? {
        try {
            val encriptado = encrypt(jarray.toString(), context.resources.getString(R.string.KeyEncripter)).replace("\n","")
            val jsObject = JSONObject()
            jsObject.put("request", encriptado)

            return jsObject
        } catch (ue: UnsupportedEncodingException) { }
        catch (ike: InvalidKeyException) { }
        catch (nae: NoSuchAlgorithmException) { }
        catch (iape: InvalidAlgorithmParameterException) { }
        catch (ibe: IllegalBlockSizeException) { }
        catch (bpe: BadPaddingException) { }
        catch (nspe: NoSuchPaddingException) { }
        catch (jex: JSONException) { }

        return null
    }

    fun jsObjectDesencriptar (valor: String, context: Context) : JSONObject? {
        try {
            val resultado = decrypt(valor, context.resources.getString(R.string.KeyEncripter))

            return JSONObject(resultado)

        } catch (ue: UnsupportedEncodingException) { }
        catch (ike: InvalidKeyException) { }
        catch (nae: NoSuchAlgorithmException) { }
        catch (iape: InvalidAlgorithmParameterException) { }
        catch (ibe: IllegalBlockSizeException) { }
        catch (bpe: BadPaddingException) { }
        catch (nspe: NoSuchPaddingException) { }
        catch (gs: GeneralSecurityException) { }
        catch (io: IOException) { }
        catch (jex: JSONException) { }
        return null
    }

    fun jsArrayDesencriptar (valor: String, context: Context) : JSONArray? {
        try {
            val resultado = decrypt(valor, context.resources.getString(R.string.KeyEncripter))

            if (resultado == "null") {
                return JSONArray()
            }
            return JSONArray(resultado)

        } catch (ue: UnsupportedEncodingException) { }
        catch (ike: InvalidKeyException) { }
        catch (nae: NoSuchAlgorithmException) { }
        catch (iape: InvalidAlgorithmParameterException) { }
        catch (ibe: IllegalBlockSizeException) { }
        catch (bpe: BadPaddingException) { }
        catch (nspe: NoSuchPaddingException) { }
        catch (gs: GeneralSecurityException) { }
        catch (io: IOException) { }
        catch (jex: JSONException) { }
        return null
    }

    fun stringDesencriptar (valor: String, context: Context) : String? {
        try {
            return decrypt(valor, context.resources.getString(R.string.KeyEncripter))

        } catch (ue: UnsupportedEncodingException) { }
        catch (ike: InvalidKeyException) { }
        catch (nae: NoSuchAlgorithmException) { }
        catch (iape: InvalidAlgorithmParameterException) { }
        catch (ibe: IllegalBlockSizeException) { }
        catch (bpe: BadPaddingException) { }
        catch (nspe: NoSuchPaddingException) { }
        catch (gs: GeneralSecurityException) { }
        catch (io: IOException) { }
        catch (jex: JSONException) { }
        return null
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    private fun decrypt(cipherText: ByteArray, key: ByteArray, initialVector: ByteArray): ByteArray {
        //var cipherText = cipherText
        val cipher = Cipher.getInstance(cipherTransformation)
        val secretKeySpecy = SecretKeySpec(key, aesEncryptionAlgorithm)
        val ivParameterSpec = IvParameterSpec(initialVector)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec)
        //cipherText = cipher.doFinal(cipherText)
        return cipher.doFinal(cipherText)
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    private fun encrypt(plainText: ByteArray, key: ByteArray, initialVector: ByteArray): ByteArray {
        //var plainText = plainText
        val cipher = Cipher.getInstance(cipherTransformation)
        val secretKeySpec = SecretKeySpec(key, aesEncryptionAlgorithm)
        val ivParameterSpec = IvParameterSpec(initialVector)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        //plainText = cipher.doFinal(plainText)
        return cipher.doFinal(plainText)
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getKeyBytes(key: String): ByteArray {
        val keyBytes = ByteArray(16)
        val parameterKeyBytes = key.toByteArray(charset(characterEncoding))
        System.arraycopy(parameterKeyBytes, 0, keyBytes, 0, Math.min(parameterKeyBytes.size, keyBytes.size))
        return keyBytes
    }

    @Throws(UnsupportedEncodingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    private fun encrypt(plainText: String, key: String): String {
        val plainTextbytes = plainText.toByteArray(charset(characterEncoding))
        val keyBytes = getKeyBytes(key)
        return Base64.encodeToString(encrypt(plainTextbytes, keyBytes, keyBytes), Base64.DEFAULT)
    }

    @Throws(KeyException::class, GeneralSecurityException::class, GeneralSecurityException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class, IOException::class)
    private fun decrypt(encryptedText: String, key: String): String {
        val cipheredBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val keyBytes = getKeyBytes(key)
        return String(decrypt(cipheredBytes, keyBytes, keyBytes), charset(characterEncoding))
    }
}