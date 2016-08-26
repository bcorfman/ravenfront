package checkers.core

import checkers.models.Animation
import checkers.models.Animation.{JumpingPiece, MovingPiece, RemovingPiece}

case class MoveAnimationPlanInput(nowTime: Double,
                                  existingAnimations: List[Animation],
                                  isComputerPlayer: Boolean,
                                  moveInfo: List[MoveInfo])

class AnimationPlanner(settings: AnimationSettings) {
  def scheduleMoveAnimations(input: MoveAnimationPlanInput): Option[List[Animation]] = {

    println(s"scheduleMoveAnimations: $input")


    def handleRemovePieces(offset: Double, incoming: List[Animation]): List[Animation] = {
      var result = incoming
      var t = input.nowTime + offset

      input.moveInfo.foreach { moveInfo =>
        moveInfo.removedPiece.foreach { rp =>
          val animation = RemovingPiece(
            piece = rp.piece,
            fromSquare = rp.squareIndex,
            startTime = input.nowTime,
            startMovingTime = t,
            endTime = t + settings.RemovePieceDurationMillis)
          result = animation :: result
          t += offset
        }
      }
      result
    }

    def handleMovePieces(incoming: List[Animation]): List[Animation] = {
      var result = incoming
      val duration = settings.MovePieceDurationMillis
      var t = input.nowTime
      input.moveInfo.foreach { moveInfo =>
        if(moveInfo.isNormalMove) {
          val animation = MovingPiece(
            piece = moveInfo.piece,
            fromSquare = moveInfo.fromSquare,
            toSquare = moveInfo.toSquare,
            startTime = t,
            duration = duration)
          result = animation :: result
          t += duration
        }
      }

      result
    }

    def handleJumpPieces(incoming: List[Animation]): List[Animation] = {
      val finalSquare = input.moveInfo.foldLeft(-1){ case (acc, moveInfo) =>
        if(moveInfo.isJump) moveInfo.toSquare
        else acc
      }

      if(finalSquare < 0) return incoming   // no jumps found

      var result = incoming
      val duration = settings.JumpPieceDurationMillis

      val baseTime = input.nowTime
      var t = baseTime
      input.moveInfo.foreach { moveInfo =>
        if(moveInfo.isJump) {
          val animation = JumpingPiece(
            piece = moveInfo.piece,
            fromSquare = moveInfo.fromSquare,
            toSquare = moveInfo.toSquare,
            finalSquare = finalSquare,
            startTime = baseTime,
            startMovingTime = t,
            endTime = t + duration
          )
          result = animation :: result
          t += duration
        }
      }

      result
    }

    def scheduleForComputer: List[Animation] = {
      var result = List.empty[Animation]

      result = handleMovePieces(result)
      result = handleJumpPieces(result)
      result = handleRemovePieces(settings.RemovePieceComputerDelayMillis, result)

      result
    }

    def scheduleForHuman: List[Animation] = {
      var result = List.empty[Animation]
      // Moving pieces or jumping pieces are not animated for humans
      result = handleRemovePieces(settings.RemovePieceHumanDelayMillis, result)
      result
    }

    val newAnimations = if (input.isComputerPlayer) scheduleForComputer else scheduleForHuman

    newAnimations match {
      case Nil => None
      case anims =>
        println(s"scheduling anims: $anims")
        Some(input.existingAnimations ++ anims)
    }
  }


}