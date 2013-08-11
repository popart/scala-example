package bootstrap.liftweb

import net.liftweb.http.LiftFilter
import javax.servlet._
import javax.servlet.http._

/*
 * Sets the run mode programatically
 * Toggle in /main/web/WEB-INF/web.xml
 */
class TestLiftFilter extends LiftFilter {
	override def init(config: FilterConfig) {
		System.setProperty("run.mode", "test")
		super.init(config)
	}
}
