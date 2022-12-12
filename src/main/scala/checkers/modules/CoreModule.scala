package checkers.modules

import checkers.computer._
import checkers.core._
import checkers.core.tables.TablesModule
import checkers.persistence.{LocalStorageNewGameSettingsPersister, NewGameSettingsPersister}
import checkers.userinterface.GameScreen
import checkers.userinterface.dialog.NewGameDialog
import checkers.util.{DomPerformanceClock, PerformanceClock}
import com.softwaremill.macwire.wire

trait CoreModule {
  protected def tablesModule: TablesModule

  protected def notation: Notation

  protected def gameScreen: GameScreen

  protected def newGameDialog: NewGameDialog

  lazy val programRegistry: ProgramRegistry = {
    val result = new ProgramRegistry
    DefaultPrograms.registerAll(result)
    result
  }

  lazy val screenLayoutSettingsProvider: ScreenLayoutSettingsProvider = wire[ScreenLayoutAdapter]

  lazy val applicationSettingsProvider: ApplicationSettingsProvider =
    ApplicationSettingsProvider(new DefaultApplicationSettings)

  lazy val animationSettings: AnimationSettings = wire[DefaultAnimationSettings]

  lazy val initialSeedsProvider: InitialSeedsProvider = DefaultInitialSeedsProvider

  lazy val moveSelectionMethodChooser: MoveSelectionMethodChooser = DefaultMoveSelectionMethodChooser

  // *** temporary for balancing  ***
  //  lazy val initialSeedsProvider: InitialSeedsProvider =
  //    StaticInitialSeedsProvider(InitialSeeds.default.copy(darkPlayer = Some(1000), lightPlayer = Some(1000)))
  //  lazy val moveSelectionMethodChooser: MoveSelectionMethodChooser = AlwaysSelectBestMove
  // ***

  lazy val boardInitializer: BoardInitializer = DefaultBoardInitializer

  //    lazy val boardInitializer: BoardInitializer = new InitializerFromBoard(Board.parseBoard(
  //      """
  //         l l l l
  //        - - - -
  //         - - - -
  //        - l l l
  //         - - - -
  //        - l l -
  //         - D - d
  //        - - - -
  //      """))

  lazy val shufflerFactory: ShufflerFactory = wire[DefaultShufflerFactory]

  lazy val mentorConfig: MentorSettings = wire[DefaultMentorSettings]

  lazy val mentorFactory: MentorFactory = wire[MentorFactory]

  lazy val scheduler: Scheduler = wire[DefaultScheduler]

  lazy val makeGameLogicModule: GameLogicModuleFactory = wire[GameLogicModuleFactory]

  lazy val gameFactory: GameFactory = wire[GameFactory]

  lazy val newGameSettingsPersister: NewGameSettingsPersister = LocalStorageNewGameSettingsPersister

  lazy val application: Application = wire[Application]

  def performanceClock: PerformanceClock = DomPerformanceClock
}
