package org.snailya

import java.lang

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{ObjectMapper, MapperFeature, DeserializationFeature}
import org.json4s.jackson.JsonMethods
import org.snailya.common._
import scala.concurrent.duration._
import java.lang.{Float => F}

import scala.concurrent.{Future, Await}
import scala.runtime.Boxed

package object exercise01 {

  val mapper = new ObjectMapper()
  mapper.registerModule(com.fasterxml.jackson.module.scala.DefaultScalaModule)

  def read(s: String) = mapper.readValue(s, classOf[Protocol])
  def write(p: Protocol) = mapper.writeValueAsString(p)


  class SquareGen {

    // generate at pos
    def gen(res: Vector[Int]): Vector[Int] = if (res.size == 36)
      res
    else {
      var i = 0
      var r: Vector[Int] = null
      val canBe =
        if (res.size % 6 == 5) {
          val mb = 111 - res.takeRight(5).sum
          if (mb <= 36 && mb > 0 && !res.contains(mb)) {
            Seq(mb)
          } else {
            Seq()
          }
        } else if (res.size > 6 * 4 && res.size < 6 * 5) {
          val cop = for (i <- 0 until res.size % 6) yield 111 - (0 until 5).map(k => res(k * 6 + i)).sum
          if (cop.forall(c => c <= 36 && c > 0 && !res.contains(c))) {
            util.Random.shuffle((1 to 36).filter(a => !res.contains(a) && !cop.contains(a)))
          } else {
            Seq()
          }
        } else {
          util.Random.shuffle((1 to 36).filter(a => !res.contains(a)))
        }
      while(i < canBe.size && r == null) {
        val tr = res :+ canBe(i)
        val rc = tr.grouped(6).toVector
        val gc = (0 until 6).map(i => {
          var s = Vector.empty[Int]
          var k = 0
          while (k < 6) {
            if (tr.size > k * 6 + i)  s = s :+ tr(k * 6 + i)
            k += 1
          }
          s
        }).toVector
        if (rc.forall(a => if (a.size == 6) a.sum == 111 else a.sum < 111) &&
          gc.forall(a => if (a.size == 6) a.sum == 111 else a.sum < 111)) {
          val g = gen(tr)
          if (g != null) {
            r = g
          }
        }
        i += 1
      }
      r
    }

    debug("gen start", System.currentTimeMillis() + "")
    val res: Vector[Int] = {
      var temp: Vector[Int] = null
      while (temp == null) {
        try {
          import scala.concurrent.ExecutionContext.Implicits.global
          temp = Await.result(Future {
            gen(Vector.empty)
          }, 4 second)
        } catch {
          case e: Throwable => Unit
        }
      }
      temp
    }

    debug("gen end", System.currentTimeMillis() + "")
  }


  type Board = Seq[Seq[Int]]

  case class GameState(board: Board,
                       score: F,
                       picks: Seq[(F, F)],
                       opicks: Seq[F],
                       reveals: Seq[(F, F)],
                       oreveals: Seq[(F, F)],
                       canFold: Boolean,
                       pot: F,
                       opot: F)

  case class RoomState(chip: F, ochip: F, state: GameState)
  
  implicit def roomToGame(r: RoomState): GameState = r.state

  def genBoard() = {
    val gen = new SquareGen
    gen.res.grouped(6).toSeq
  }



  // implicit def floatToJavaFloat(f: Float) = new java.lang.Float(f)
  implicit def javaFloatPairToFloatPair(f: (F, F)) = (f._1.floatValue(), f._2.floatValue())
  implicit def floatPairToJavaFloatPair(f: (Float, Float)): (F, F) = (new java.lang.Float(f._1), new java.lang.Float(f._2))
  implicit def floatSeqToJavaFloatSeq(s: Seq[Float]): Seq[F] = s.map(a => (a: F))
  implicit def floatPairSeqToJavaFloatPairSeq(s: Seq[(Float, Float)]): Seq[(F, F)] = s.map(a => (a: (F, F)))


  case class Register(name: String)
  case class U()
  case class Protocol(register: Register = null,
                      newMatch: U = null,
                      userLeaved: U = null,
                      newGame: RoomState = null,
                      pick: (F, F) = null,
                      opick: RoomState = null,
                      reveal: F = null,
                      oreveal: RoomState = null,
                      bet: F = null,
                      obet: RoomState = null,
                      fold: U = null,
                      end: RoomState = null,
                      leave: U = null)
}
