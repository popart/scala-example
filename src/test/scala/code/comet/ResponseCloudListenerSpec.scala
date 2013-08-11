package code
package snippet

import code.model._
import code.comet._
import net.liftweb.http._
import net.liftweb.mapper.BaseMetaMapper
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import net.liftweb.mapper._
import Helpers._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.xml._

class ResponseCloudCometSpec extends Spec with ShouldMatchers with SessionContext {
  describe("A ResponseCloudListener") {
		it ("should instantiate and register with running session's ResponseServer") {
			DBInit.setupSession
			val dl = new ResponseCloudListener
			//dl.registerWith should not be null
			Some(dl.registerWith) should equal (Session.getResponseCloudServer(User.getSessionId))
		}
		it ("should correctly count words") {
			var cloudListener = new ResponseCloudListener
			val wc1 = new WordCounter("a B b. c")
			assert(wc1.getWordCount.get("a") === Some(1))
			assert(wc1.getWordCount.get("b") === Some(2))
			val wc2 = new WordCounter("b c d")
			val weighted = cloudListener.weightTopXWords(List(wc1, wc2), 10)
			//not going to test the actualy floated weight b/c rounding differences
			assert(weighted(0)._1 === "b")
			assert(weighted(1)._1 === "c")
		}
  }
	describe("A ResponseServer") {
		it ("should be cleared from memory when a session ends") {
			DBInit.setupSession
			User.clearSession(true)
			Session.getResponseCloudServer(User.getSessionId) should be (None)
		}
	}
}
