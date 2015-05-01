package org.snailya.exercise01

import java.net.URI

import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx._
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.{Color, GL20, Pixmap, Texture}
import com.badlogic.gdx.utils.Align
import io.backchat.hookup.HookupClient.Receive
import io.backchat.hookup._
import org.snailya.common._
import rx.lang.scala.{Observable, Subscription}

import scala.concurrent.duration._
import scala.util.Random
class Logic extends HookupClient with StateMachine {
  logic =>

  implicit val formats = org.json4s.DefaultFormats


  val preference = Gdx.app.getPreferences("Exercise01")

  val name = {
    val a = preference.getString("n")
    val res = if (a == null || a.isEmpty || true) {
      val rand = (0 until 16).map(a => Random.nextInt(10)).mkString
      debug("new name", rand)
      preference.putString("n", rand).flush()
      rand
    } else {
      a
    }
    debug("name", res)
    res
  }

  val localUrl = "ws://localhost:8127/"
  val remoteUrl = "ws://54.68.195.171:8127/"

  override def settings: HookupClientConfig = HookupClientConfig(
    protocols = Seq(new TextProtocol()),
    defaultProtocol = new TextProtocol(),
    uri = URI.create(localUrl),
    throttle = MaxTimesThrottle(2 seconds, 10 seconds, 2),
    buffer = Some(new FileBuffer(Gdx.files.internal("buffer.log").file())))


  override def receive: Receive = {
    case TextMessage(t) =>
      debug("text message@" + current, t)
      current.onEvent lift read(t)
    case Connected =>
      current.onEvent(Connected)
    case a if a.isInstanceOf[Disconnected] || a.isInstanceOf[Error] =>
      debug("disconnected")
      current.onEvent orElse ({
        case _ => goTo(start)
      }: OnEvent) lift a
  }

  override val start = new State {
    override def whenEnter() = goTo(waitConnect)
  }

  val waitConnect: State = new State {
    override def whenEnter(): Unit = connect()

    override def onEvent: OnEvent = {
      case Connected => goTo(hall)
      case Error(_) =>
        goTo(disconnected)
        debug("waitConnect error")
    }
  }

  val disconnected = new State {
    override def onEvent: OnEvent = {
      case Unit => goTo(start)
    }
  }

  val hall: State = new State {
    override def onEvent: OnEvent = {
      case Unit => goTo(waiting)
    }
  }

  val waiting = new State {
    override def whenEnter(): Unit = send(write(Protocol(register = Register(name))))

    override def onEvent: OnEvent = {
      case p: Protocol if p.newMatch != null =>
        goTo(gaming)
      case Unit =>
        send(write(Protocol(leave = U())))
        goTo(hall)
    }
  }


  val gaming = new State with StateMachine {

    var state: RoomState = null

    // is this correct??

    override def whenEnter(): Unit = {
      state = null
      super.whenEnter()
    }

    override def onEvent: OnEvent = ({
      case p: Protocol if p.userLeaved != null =>
        logic.goTo(logic.hall)
      case Unit =>
        send(write(Protocol(leave = U())))
        logic.goTo(hall)
      case p: Protocol if p.end != null =>
        state = p.end
        goTo(end)
    }: OnEvent).orElse(super.onEvent)

    val start: State = new State {
      override def onEvent: OnEvent = {
        case p: Protocol if p.newGame != null =>
          state = p.newGame
          goTo(pick)
      }
    }


    val pick = new State {
      override def onEvent: OnEvent = {
        case pks: (Float, Float) =>
          send(write(Protocol(pick = pks)))
          goTo(waitPick)
      }
    }

    val waitPick = new State {
      override def onEvent: OnEvent = {
        case p: Protocol if p.opick != null =>
          state = p.opick
          goTo(bet)
      }
    }

    val bet: State = new State {
      override def onEvent: OnEvent = {
        case i: java.lang.Float =>
          send(write(Protocol(bet = i)))
          goTo(waitBet)
        case Unit =>
          send(write(Protocol(fold = U())))
      }
    }

    val waitBet = new State {
      override def onEvent: OnEvent = {
        case p: Protocol if p.obet != null =>
          state = p.obet
          if (state.pot == state.opot) {
            if (state.picks.size == 3 && state.opicks.size == 3) {
              // the end will not make here!!!
              goTo(reveal)
            } else {
              goTo(pick)
            }
          } else if (state.pot < state.opot) {
            goTo(bet)
          }
      }
    }

    val reveal = new State {
      override def onEvent: OnEvent = {
        case i: Float =>
          send(write(Protocol(reveal = i)))
          goTo(waitReveal)
      }
    }

    val waitReveal = new State {
      override def onEvent: OnEvent = {
        case p: Protocol if p.oreveal != null =>
          state = p.oreveal
          goTo(bet)
      }
    }

    val end = new State {
      var events: Seq[Any] = Seq.empty
      override def whenEnter(): Unit = {
        Observable.just(0).delay(5 second).subscribe(i => {
          goTo(start)
          events.foreach(e => start.onEvent lift e)
          events = Seq.empty
        })
      }

      override def onEvent: OnEvent = {
        case a => events = events :+ a
      }
    }
  }

}


class Cmc extends Game {

  // general draw stuffs
  implicit def fun2ScreenAdapter(f: Float => Unit): Screen = new ScreenAdapter {
    override def render(delta: Float): Unit = {
      f(delta)
    }
  }

  def texture_colorSquare(i: Color) = {
    val pixmap = new Pixmap(1, 1, Format.RGBA4444)
    pixmap.setColor(i)
    pixmap.fill()
    val texture = new Texture(pixmap)
    pixmap.dispose()
    texture
  }

  def draw_clear() = {
    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
  }


  lazy val logic = new Logic

  override def create(): Unit = {
    // game general


    // size
    val ssize = 640

    val padding = ssize / 5
    val bsize = padding * 3
    val csize = bsize / 6



    // draw, pos

    val batch = new SpriteBatch()

    val color = new {
      val green = new Color(0x62851eff)
      val red = new Color(0xa9112cff)
      val yellow = new Color(0xca9862cc)
    }

    val font = new {
      val gen = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"))

      def ofAttr(i: Int, c: Color = Color.WHITE) = {
        val p = new FreeTypeFontParameter
        p.size = i
        p.color = c
        gen.generateFont(p)
      }

      val px36 = ofAttr(36)
      val px24 = ofAttr(24)
      val red = ofAttr(24, color.red)
      val green = ofAttr(24, color.green)
      val black = ofAttr(24, Color.BLACK)
      gen.dispose()
    }

    val texture = new {
      val green3 = texture_colorSquare(color.green.mul(1, 1, 1, 0.3f))
      val red3 = texture_colorSquare(color.red.mul(1, 1, 1, 0.3f))
      val green = texture_colorSquare(color.green)
      val red = texture_colorSquare(color.red)
      val dark = texture_colorSquare(Color.BLACK.mul(1, 1, 1, 0.5f))
      val yellow = texture_colorSquare(color.yellow)
    }


    def draw_textCentered(s: String, font: BitmapFont, left: Int, yc: Int, width: Int) = {
      font.draw(batch, s, left, yc + font.getAscent, width, Align.center, true)
    }

    def draw_textFullScreen(s: String): Unit = {
      draw_textCentered(s, font.px36, 0, ssize / 2, ssize)
    }

    case class Button(var text: String, width: Int, height: Int, x: Int, y: Int, bracked: Boolean = true) {
      def draw() =
        draw_textCentered(if (bracked) "[" + text + "]" else text, font.px24, x, y + height / 2, width)

      def clicked() = Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.LEFT) &&
        Gdx.input.getX >= x &&
        Gdx.input.getX <= x + width &&
        (ssize - Gdx.input.getY) >= y &&
        (ssize - Gdx.input.getY) <= y + height

      var visible = true
    }


    // input

    def key(i: Int) = Gdx.input.isKeyJustPressed(i)

    logic.enter()
    // game

    val g = logic.gaming

    logic.changes.subscribe(s => {
      s match {
        case logic.start =>
        case logic.waitConnect =>
          setScreen((d: Float) => {
            draw_clear()
            batch.begin()
            draw_textFullScreen("connecting")
            batch.end()
          })
        case logic.disconnected =>
          setScreen((d: Float) => {
            if (Gdx.input.justTouched()) {
              logic.disconnected.onEvent(Unit)
            }
            draw_clear()
            batch.begin()
            draw_textFullScreen("retry connect")
            batch.end()
          })
        case logic.hall =>
          val startGame = Button("start game", csize * 4, bsize / 3, csize + padding, bsize / 4 + padding)
          setScreen((d: Float) => {
            if (startGame.clicked()) {
              logic.hall.onEvent(Unit)
            }
            draw_clear()
            batch.begin()
            startGame.draw()
            draw_textCentered("a CORDIAL MINUET clone", font.px36, padding, ssize - padding - bsize / 4, bsize)
            batch.end()
          })
        case logic.waiting =>
          val cancel = Button("cancel", csize * 4, bsize / 3, csize + padding, padding)
          setScreen((d: Float) => {
            draw_clear()
            batch.begin()
            if (cancel.clicked()) {
              logic.waiting.onEvent(Unit)
            }
            cancel.draw()
            draw_textFullScreen("waiting for player")
            batch.end()
          })
        case logic.gaming.reveal =>
        case logic.gaming.waitReveal =>
        case logic.gaming.end =>
        case logic.gaming =>
          val OLEFT = -0.5f
          val ORIGHT = 5.5f

          var pickGreen = OLEFT
          var pickRed = ORIGHT

          var pickAdd = 0f
          var minAdd = 0f

          def normalize(f: Float) = if (f < 0) -1f else if (f > 5) 6f else f
          def denormalize(f: Float) = if (f < 0) OLEFT else if (f > 5) ORIGHT else f
          def valid(f: Float) = f != OLEFT && f != ORIGHT

          def nc(i: Float): Int = (padding + i * csize).toInt

          val commit = Button("commit", bsize / 2, padding, padding, padding + bsize)
          val leave = Button("leave", bsize / 2, padding, padding + bsize / 2, padding + bsize)
          val number = Button("", csize, padding, padding + csize, 0, bracked = false)
          val bet = Button("bet", csize * 2, padding, padding + csize * 2, 0)
          val fold = Button("fold", csize * 2, padding, padding + csize * 4, 0)

          var subs: Subscription = null
          subs = g.changes.subscribe(s => s match {
            case g.start =>
            case g.pick =>
              pickGreen = OLEFT
              pickRed = ORIGHT
            case g.waitPick =>
            case g.bet =>
              pickAdd = 0
              minAdd = Math.max(g.state.opot - g.state.pot, 0)
            case g.waitBet =>
            case g.reveal =>
              pickGreen = OLEFT
            case g.waitReveal =>
            case g.end =>
              subs.unsubscribe()
          })
          setScreen((d: Float) => {
            draw_clear()
            if (g.state == null) {
            } else {
              batch.begin()
              g.current match {
                case g.pick =>
                  if (key(Input.Keys.J)) {
                    pickGreen = denormalize(normalize(pickGreen) - 1)
                  } else if (key(Input.Keys.K)) {
                    pickGreen = denormalize(normalize(pickGreen) + 1)
                  } else if (key(Input.Keys.H)) {
                    pickRed = denormalize(normalize(pickRed) - 1)
                  } else if (key(Input.Keys.L)) {
                    pickRed = denormalize(normalize(pickRed) + 1)
                  }

                  commit.visible =
                    valid(pickGreen) && valid(pickRed) && pickGreen != pickRed && {
                      val current = g.state.picks.map(a => Seq(a._1, a._2)).flatten.toSeq
                      !current.contains(pickGreen) && !current.contains(pickRed)
                    }

                  if (commit.visible) commit.draw()

                  draw_textCentered("i", font.green, (padding + pickGreen * csize).toInt, (padding + OLEFT * csize).toInt, csize)
                  draw_textCentered("o", font.red, (padding + pickRed * csize).toInt, (padding + OLEFT * csize).toInt, csize)

                  if (commit.visible && commit.clicked()) g.pick.onEvent((pickGreen, pickRed))
                case g.reveal =>
                  if (key(Input.Keys.J)) {
                    pickGreen = denormalize(normalize(pickGreen) - 1)
                  } else if (key(Input.Keys.K)) {
                    pickGreen = denormalize(normalize(pickGreen) + 1)
                  }

                  draw_textCentered("i", font.green, (padding + pickGreen * csize).toInt, (padding + OLEFT * csize).toInt, csize)

                  commit.visible = valid(pickGreen) && g.state.picks.map(_._1).contains(pickGreen) && !g.state.reveals.map(_._1).contains(pickGreen)
                  if (commit.visible) commit.draw()

                  if (commit.visible && commit.clicked()) g.reveal.onEvent(pickGreen)
                case g.waitPick =>
                case g.bet =>
                  if (key(Input.Keys.H)) {
                    pickAdd = pickAdd - 10
                  } else if (key(Input.Keys.L)) {
                    pickAdd = Math.min(g.state.chip, pickAdd + 10)
                  } else if (key(Input.Keys.J)) {
                    pickAdd = pickAdd - 1
                  } else if (key(Input.Keys.K)) {
                    pickAdd = Math.min(g.state.chip, pickAdd + 1)
                  }
                  if (pickAdd < minAdd) pickAdd = minAdd

                  if (g.state.canFold) fold.draw()
                  bet.draw()

                  number.text = pickAdd.toString
                  number.draw()

                  if (bet.clicked()) g.bet.onEvent(pickAdd)

                  if (g.state.canFold && fold.clicked()) g.bet.onEvent(Unit)
                case g.waitReveal =>
                case g.end =>
                case _ => Unit
              }

              // yellow
              batch.draw(texture.yellow, padding, padding, bsize, bsize)

              // draw green picks
              for (i <- g.state.picks) for (j <- 0 until 6) batch.draw(texture.green3, nc(i._1), nc(j), csize, csize)
              for (j <- g.state.opicks) for (i <- 0 until 6) batch.draw(texture.green3, nc(i), nc(j), csize, csize)

              // draw red picks
              for (i <- g.state.picks) for (j <- 0 until 6) batch.draw(texture.red3, nc(i._2), nc(j), csize, csize)

              // draw cross offs
              for (k <- 0 until g.state.picks.size) {
                val p = g.state.picks(k)._1
                val op = g.state.opicks(k)
                for (j <- 0 until 6) if (j != op) batch.draw(texture.dark, nc(p), nc(j), csize, csize)
                for (i <- 0 until 6) if (i != p) batch.draw(texture.dark, nc(i), nc(op), csize, csize)
              }

              // draw numbers
              if (g.state.board != null) {
                for (i <- 0 until 6) {
                  for (j <- 0 until 6) {
                    draw_textCentered(g.state.board(i)(j).toString, font.black, padding + i * csize, padding + j * csize + csize / 2, csize)
                  }
                }
              }

              // draw reveals
              for (o <- g.state.reveals) {
                for (i <- 0 until 6) if (i != o._1) batch.draw(texture.dark, nc(i), nc(o._2), csize, csize)
                for (j <- 0 until 6) if (j != o._2) batch.draw(texture.dark, nc(o._1), nc(j), csize, csize)
                batch.draw(texture.green3, nc(o._1), nc(o._2), csize, csize)
              }
              for (o <- g.state.oreveals) {
                for (i <- 0 until 6) if (i != o._1) batch.draw(texture.dark, nc(i), nc(o._2), csize, csize)
                for (j <- 0 until 6) if (j != o._2) batch.draw(texture.dark, nc(o._1), nc(j), csize, csize)
                batch.draw(texture.red3, nc(o._1), nc(o._2), csize, csize)
              }

              // draw left
              draw_textCentered(g.state.opot.toString, font.red, 0, ssize / 2 + csize / 2, padding)
              draw_textCentered(g.state.pot.toString, font.green, 0, ssize / 2 - csize / 2, padding)
              draw_textCentered(g.state.ochip.toString, font.red, 0, padding + bsize - padding / 4, padding)
              draw_textCentered(g.state.chip.toString, font.green, 0, padding + padding / 4, padding)

              // draw right


              // draw score

              draw_textCentered(g.state.chip.toString, font.green, 0, padding + padding / 4, padding)

              // on leave click
              if (leave.clicked()) {
                g.onEvent(Unit)
              }
              leave.draw()

              batch.end()
            }
          })
      }
    })
  }

  override def dispose(): Unit = {
    System.exit(0)
  }
}
