package checkers.components.chrome

import checkers.consts._
import checkers.core.{GameModelReader, PlayerDescription}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object TopChrome {

  case class Props(gameModel: GameModelReader,
                   widthPixels: Int,
                   heightPixels: Int)

  class TopChromeBackend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {
      val panelWidth = props.widthPixels / 2
      val panelHeight = props.heightPixels

      def createPanelProps(color: Color, player: PlayerDescription) = {
        val model = props.gameModel
        val isPlayerTurn = model.displayTurnToMove == color
        val endingTurn = model.inputPhase.endingTurn
        val jumpIndicator = isPlayerTurn && (!endingTurn) && model.playerMustJump
        val scoreDisplay = if(model.scoreDisplayEnabled) {
          Some(model.getScore(color).toString)
        } else None

        PlayerPanel.Props(
          widthPixels = panelWidth,
          heightPixels = panelHeight,
          color = color,
          playerName = player.displayName,
          isComputerPlayer = player.isComputer,
          clockDisplay = "---",
          scoreDisplay = scoreDisplay,
          isPlayerTurn = isPlayerTurn,
          endingTurn = endingTurn,
          jumpIndicator = jumpIndicator,
          thinkingIndicator = false
        )
      }

      def makePanel(panelProps: PlayerPanel.Props, translateX: Int, translateY: Int) = {
        <.svg.g(
          ^.svg.transform := s"translate($translateX,$translateY)",
          PlayerPanel(panelProps)
        )
      }

      val darkProps = createPanelProps(DARK, props.gameModel.darkPlayer)
      val darkPanel = makePanel(darkProps, 0, 0)

      val lightProps = createPanelProps(LIGHT, props.gameModel.lightPlayer)
      val lightPanel = makePanel(lightProps, panelWidth, 0)

      <.svg.svg(
        ^.id := "top-chrome",
        ^.svg.width := s"${props.widthPixels}px",
        ^.svg.height := s"${props.heightPixels}px",
        //^.onMouseMove ==> handleMouseMove,
        //SceneFrame((props._1, props._2, state))
        darkPanel,
        lightPanel
      )
    }
  }

  val component = ReactComponentB[Props]("TopChrome")
    .renderBackend[TopChromeBackend]
    //    .shouldComponentUpdateCB { case ShouldComponentUpdate(scope, nextProps, _) =>
    //      val result = scope.props != nextProps
    //      CallbackTo.pure(result)
    //    }
    .build

  def apply(props: Props) = component(props)


}