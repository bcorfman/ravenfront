package checkers.core

class MoveExecutor(rulesSettings: RulesSettings) {

  /**
    * Updates the board in place.  Does not return metadata, other than a flag indicating a crowning event.
    * @return if true, move ended in a piece being crowned
    */
  def fastExecute(boardState: MutableBoardState, move: Move): Boolean = {
    var crowned = false

    def runSimple(move: SimpleMove): Unit = {
      val piece = boardState.getOccupant(move.from)
      if(move.over >= 0) boardState.setOccupant(move.over, Empty)
      boardState.setOccupant(move.from, Empty)
      if(Board.isCrowningMove(piece, move.to)) {
        crowned = true
        boardState.setOccupant(move.to, piece.crowned)
      }
    }

    move match {
      case move: SimpleMove => runSimple(move)
      case CompoundMove(moves) => moves.foreach(runSimple)
    }
    crowned
  }


}