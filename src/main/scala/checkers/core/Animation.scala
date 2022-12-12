package checkers.core

import checkers.consts._

sealed trait Animation {
  def startTime: Double

  def duration: Double

  def linearProgress(nowTime: Double): Double

  def hasStarted(nowTime: Double): Boolean = nowTime >= startTime

  def isExpired(nowTime: Double): Boolean

  def isActive(nowTime: Double): Boolean = !isExpired(nowTime)

}

object Animation {

  /**
    * An animation that temporarily hides one of the static pieces on the board (such as when moving a piece)
    */
  trait HidesStaticPiece {
    def hidesPieceAtSquare: Int
  }

  trait OneTimeAnimation extends Animation {
    def linearProgress(nowTime: Double): Double =
      if(nowTime <= startTime) 0.0
      else math.min((nowTime - startTime) / duration, 1.0)

    def isExpired(nowTime: Double): Boolean =
      (nowTime - startTime) >= duration
  }

  case class MovingPiece(piece: Occupant,
                         fromSquare: Int,
                         toSquare: Int,
                         startTime: Double,
                         duration: Double) extends OneTimeAnimation with HidesStaticPiece {
    def hidesPieceAtSquare: Int = toSquare
  }

  trait OneTimeDeferredAnimation extends OneTimeAnimation {
    def startMovingTime: Double
    def endTime: Double

    val duration: Double = endTime - startTime
    val moveDuration: Double = endTime - startMovingTime

    override def linearProgress(nowTime: Double): Double = {
      if(moveDuration <= 0) 1.0
      else if(nowTime <= startMovingTime) 0.0
      else math.min((nowTime - startMovingTime) / moveDuration, 1.0)
    }

    override def isExpired(nowTime: Double): Boolean = nowTime >= endTime
  }

  case class JumpingPiece(piece: Occupant,
                          fromSquare: Int,
                          toSquare: Int,
                          finalSquare: Int,
                          startTime: Double,
                          startMovingTime: Double,
                          endTime: Double,
                          isFirst: Boolean) extends OneTimeDeferredAnimation with HidesStaticPiece {
    def hidesPieceAtSquare: Int = finalSquare

    def isLast: Boolean = toSquare == finalSquare

    def isPieceVisible(nowTime: Double): Boolean =
      isFirst || (nowTime >= startMovingTime)
  }

  case class RemovingPiece(piece: Occupant,
                           fromSquare: Int,
                           startTime: Double,
                           startMovingTime: Double,
                           endTime: Double) extends OneTimeDeferredAnimation

  case class PlacingPiece(piece: Occupant,
                          toSquare: Int,
                          startTime: Double,
                          startMovingTime: Double,
                          endTime: Double) extends OneTimeDeferredAnimation with HidesStaticPiece {
    def hidesPieceAtSquare: Int = toSquare
  }

  case class IllegalPieceSelection(piece: Occupant,
                                   squareIndex: Int,
                                   startTime: Double,
                                   startMovingTime: Double,
                                   endTime: Double) extends OneTimeDeferredAnimation with HidesStaticPiece {
    def hidesPieceAtSquare: Int = squareIndex
  }

  case class CrowningPiece(side: Side,
                           squareIndex: Int,
                           startTime: Double,
                           startMovingTime: Double,
                           endTime: Double) extends OneTimeDeferredAnimation with HidesStaticPiece {
    def hidesPieceAtSquare: Int = squareIndex
  }

  case class HintAnimation(fromSquare: Int,
                           toSquare: Int,
                           flashDuration: Double,
                           startTime: Double,
                           duration: Double) extends OneTimeAnimation

  case class RotatingBoardAnimation(startTime: Double,
                                    duration: Double) extends OneTimeAnimation


}