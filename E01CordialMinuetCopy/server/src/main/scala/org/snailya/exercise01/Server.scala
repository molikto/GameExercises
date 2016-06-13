package  com.snailya.exercise01

import java.util.concurrent.ConcurrentHashMap

import io.backchat.hookup.HookupClient.Receive
import io.backchat.hookup.HookupServer.HookupServerClient
import io.backchat.hookup._
import io.backchat.hookup.examples.DefaultConversions
import org.json4s
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.snailya.common._
import org.snailya.common.{OnEvent, StateMachine}
import org.snailya.exercise01._

import scala.reflect.Manifest


object Server extends App {

  implicit val formats = org.json4s.DefaultFormats

  val lock = new Object

  var users = Map.empty[String, Client]

  class Room(val left: Client, val right: Client) extends StateMachine {

    class RoundState() {
      var board: Board = null
      var boardRight: Board = null

      var lpicks, rpicks: Seq[(Float, Float)] = Seq.empty
      var lreveals, rreveals: Seq[(Float, Float)] = Seq.empty
      var lpot, rpot: Float = 0
      var lbet, rbet: Float = 0
      var lpb, rpb: Boolean = false

      def lscore: Float =  lpicks.zip(rpicks).map(k => board(k._1._1.toInt)(k._2._2.toInt)).sum
      def rscore: Float =  rpicks.zip(lpicks).map(k => boardRight(k._1._1.toInt)(k._2._2.toInt)).sum
      
      def lstate = GameState(board, lscore, lpicks, rpicks.map(_._2), lreveals, rreveals.map(a => (a._2, a._1)), lpot < rpot, lpot, rpot)

      def rstate = GameState(boardRight, rscore, rpicks, lpicks.map(_._2), rreveals, lreveals.map(a => (a._2, a._1)), rpot < lpot, rpot, lpot)
    }
    var lchip, rchip: Float = 100

    var state: RoundState = null


    def newRound() = {
      state = new RoundState()
      lchip -= 1
      rchip -= 1
      state.lpot = 1
      state.rpot = 1
      state.board = genBoard()

      state.boardRight = (0 until 6).map(k => {
        (0 until 6).map(i => state.board(i)(k))
      })
    }

    def lstate = {
      RoomState(lchip, rchip, state.lstate)
    }

    def rstate = {
      RoomState(rchip, lchip, state.rstate)
    }

    override val start = new State {
      override def whenEnter(): Unit = {
        left.send(write(Protocol(newMatch = U())))
        right.send(write(Protocol(newMatch = U())))
        goTo(gaming)
      }
    }


    // is this correct??
    override def onEvent: OnEvent = ({
      case (a@Disconnected(_), l: Boolean)=>
        val alive = if (l) right else left
        alive.send(write(Protocol(userLeaved = U())))
        alive.goTo(alive.start)
      case (p: Protocol, l: Boolean) if p.leave != null =>
        val alive = if (l) right else left
        alive.send(write(Protocol(userLeaved = U())))
        left.goTo(left.start)
        right.goTo(right.start)
    } : OnEvent).orElse(super.onEvent)

    val gaming: State = new State {

      override def whenEnter(): Unit = {
        newRound()
        left.send(write(Protocol(newGame = lstate)))
        right.send(write(Protocol(newGame = rstate)))
      }

      override def onEvent: OnEvent = {
        case (p: Protocol, l: Boolean) =>
          if (p.pick != null) {
            if (l) state.lpicks = state.lpicks :+ (p.pick: (Float, Float))
            else state.rpicks = state.rpicks :+ (p.pick: (Float, Float))
            if (state.lpicks.size == state.rpicks.size) {
              left.send(write(Protocol(opick = lstate)))
              right.send(write(Protocol(opick = rstate)))
            }
          } else if (p.bet != null) {
            if (l) state.lbet += p.bet
            else state.rbet += p.bet
            if (l) state.lpb = true else state.rpb = true
            if (state.lpb && state.rpb) {
              lchip -= state.lbet
              rchip -= state.rbet
              state.lpot += state.lbet
              state.rpot += state.rbet
              state.lbet = 0
              state.rbet = 0
              if (state.lpot == state.rpot) {
                state.lpb = false
                state.rpb = false
              }
              left.send(write(Protocol(obet = lstate)))
              right.send(write(Protocol(obet = rstate)))
            }
          } else if (p.reveal != null) {
            if (l) {
              val k = state.rpicks(state.lpicks.zipWithIndex.find(_._1._1 == p.reveal).get._2)._2
              state.lreveals = state.lreveals :+ ((p.reveal: Float), k)
            } else {
              val k = state.lpicks(state.rpicks.zipWithIndex.find(_._1._1 == p.reveal).get._2)._2
              state.rreveals = state.rreveals :+ ((p.reveal: Float), k)
            }
            if (state.lreveals.size == state.rreveals.size) {
              if (state.rreveals.size == 2) {
                if (state.lscore > state.rscore) {
                  lchip += state.lpot + state.rpot
                } else if (state.rscore > state.lscore) {
                  rchip += state.lpot + state.rpot
                } else {
                  lchip += state.lpot
                  rchip += state.rpot
                }
                state.lpot = 0
                state.rpot = 0
                left.send(write(Protocol(end = lstate)))
                right.send(write(Protocol(end = rstate)))
                goTo(start)
              } else {
                left.send(write(Protocol(oreveal = lstate)))
                right.send(write(Protocol(oreveal = rstate)))
              }
            }
          } else if (p.fold != null) {
            if (l) rchip += state.lpot + state.rpot else lchip += state.lbet + state.rpot
            state.lpot = 0
            state.rpot = 0
            left.send(write(Protocol(end = lstate)))
            right.send(write(Protocol(end = rstate)))
            goTo(gaming)
          }
      }
    }
  }










  class Client extends HookupServerClient with StateMachine {
    self =>

    var name: String = null
    var room: (Room, Boolean) = null

    override val start = new State {
      override def onEvent: OnEvent = {
        case p: Protocol if p.register != null =>
          name = p.register.name
          var other: Client = null
          lock.synchronized {
            if (users.isEmpty) {
              users += name -> self
            } else {
              val p = users.head
              users -= p._1
              other = p._2
            }
            if (other != null) {
              debug("new room")
              val r = new Room(self, other)
              room = (r, true)
              other.room = (r, false)
              room._1.enter()
              goTo(inRoom)
              other.goTo(other.inRoom)
            }
          }
        case p: Protocol if p.leave != null =>
          if (name != null) users -= name
      }
    }

    val inRoom = new State {
      override def onEvent: OnEvent = {
        case a => room._1.synchronized {
          room._1.onEvent lift (a, room._2)
        }
      }

      override def whenExit(): Unit = room = null
    }

    override def receive: Receive = {
      case Connected =>
        debug("connected", id.toString)
      case dis@Disconnected(_) =>
        debug("disconnected", id.toString)
        current.onEvent lift dis
        if (name != null) users -= name
      case TextMessage(t) =>
        debug("text message " + this.id, t)
        val p = read(t)
        current.onEvent lift p
      case Error(e) => e.foreach(_.printStackTrace())
      case a => debug("unknown message", a.toString)
    }

  }













  HookupServer(new ServerInfo(name = "CmcServer", port = 8127, defaultProtocol = "textProtocol", capabilities = Seq(SubProtocols(new TextProtocol())))) {
    {
      val c = new Client
      c.enter()
      c
    }
  }.start
}

