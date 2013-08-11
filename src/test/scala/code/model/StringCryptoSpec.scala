package code
package model

import net.liftweb.http._
import net.liftweb.mapper.BaseMetaMapper
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.mockweb.MockWeb
import code.lib._
import net.liftweb.mapper._
import Helpers._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.xml._

class StringCryptoSpec extends Spec with ShouldMatchers {
  describe("The Crypto tool") {
		it ("should encrypt and then decrypt into the same strings") {
			val testStrings = 
				" " ::
				"`~!@#$%^&*()" ::
				"<>,./?';\"[]{}\\|" ::
				"asdfjkl; asdfkjk; " ::
				" \n" :: Nil
			testStrings.foreach(ts =>
				StringCrypto.decrypt(StringCrypto.encrypt(ts)) should equal (ts)
			)
		}
  }
}
