package checkers

import checkers.core.experimental.SimpleMoveIndex
import checkers.driver.GameScreenDriver
import checkers.logger._
import checkers.models.{GameScreenModel, GameSettings}
import checkers.style.GlobalStyles
import checkers.util.DebugUtils
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalacss.Defaults._
import scalacss.ScalaCssReact._

@JSExport("CheckersMain")
object CheckersMain extends js.JSApp {

  @JSExport
  def main(): Unit = {
    log.warn("Application starting")

    // create stylesheet
    GlobalStyles.addToDocument()

    val host = dom.document.getElementById("root")
    sandbox1(host)
  }

  private def sandbox1(host: dom.Node): Unit = {
    DebugUtils.log(SimpleMoveIndex.index)

    val model = GameScreenModel.initial(GameSettings.default)
    val driver = new GameScreenDriver(host, model)
    driver.run()
  }
}
