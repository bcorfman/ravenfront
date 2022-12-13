package checkers.core

import checkers.consts._
import checkers.test.TestSuiteBase
import utest._

import scala.util.Random

object BoardStateTests extends TestSuiteBase {

  private val allPieces = List(LIGHTMAN, DARKMAN, LIGHTKING, DARKKING)
  private val allSquares = Board.playableSquares.toSet

  private def shuffledSquares() = Random.shuffle(Board.playableSquares.toList)

  private def randomSquares(count: Int) = shuffledSquares().take(count)


  val tests: Tests = Tests {
    test("BoardState") {
      test("PlacePieces") {
        val squares = randomSquares(4)
        val placements = squares.zip(allPieces)
        val bs = placements.foldLeft(BoardState.empty) {
          case (result, (square, piece)) => result.updated(square, piece)
        }

        // pieces in correct place
        placements.foreach {
          case (square, piece) =>
            val occupant = bs.getOccupant(square)
            assert(occupant == piece)
        }

        // all other squares are empty
        (allSquares -- squares.toSet).foreach { square =>
          assert(bs.isSquareEmpty(square))
        }
      }
    }
  }
}
