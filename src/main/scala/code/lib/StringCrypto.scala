package code
package lib

import javax.crypto._
import javax.crypto.spec._
import sun.misc.BASE64Encoder
import sun.misc.BASE64Decoder

/*
 * Some hard coded crypto to meet IES requirements
 * used on student names
 */
object StringCrypto {
	//DO NOT CHANGE THIS KEY
	// "4f8u#u)iu%or!`CB"
	val staticKeyStr = "4f8u#u)iu%or!`CB"
	//val keySpec = new SecretKeySpec(staticKeyStr.getBytes, "Blowfish")
	val secretKey = new SecretKeySpec(staticKeyStr.getBytes, "Blowfish")
	//val factory = SecretKeyFactory.getInstance("Blowfish")
	//val secretKey = factory.generateSecret(keySpec)
	
	val cipherIn = Cipher.getInstance("Blowfish")
	cipherIn.init(Cipher.ENCRYPT_MODE, secretKey)
	
	val cipherOut = Cipher.getInstance("Blowfish")
	cipherOut.init(Cipher.DECRYPT_MODE, secretKey)

	val encoder = new BASE64Encoder
	val decoder = new BASE64Decoder

	def encrypt(in: String): String = {
		val plainBytes = in.getBytes()	
		val cryptedBytes = cipherIn.doFinal(plainBytes)
		encoder.encode(cryptedBytes)
	}

	def decrypt(out: String): String = {
		val cryptedBytes = decoder.decodeBuffer(out)
		val plainBytes = cipherOut.doFinal(cryptedBytes)
		new String(plainBytes)
	}
}
