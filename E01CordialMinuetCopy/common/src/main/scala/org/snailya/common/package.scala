
package org.snailya

import rx.lang.scala.subjects.BehaviorSubject


package object common {

  def debug(str: String) = println(str)
  def debug(n: String, s: String) = println(n + ": " + s)

  type OnEvent = PartialFunction[Any, Unit]

  trait State {
    def whenEnter(): Unit = Unit
    def whenExit(): Unit = Unit
    def onEvent: OnEvent = {
      case _ => Unit
    }
  }


  trait StateMachine extends State {
    val start: State
    def current: State = cur
    private var cur: State = null
    var prev: State = null

    val changes = BehaviorSubject[State]()

    def goTo(state: State) = {
      debug("state change", state.toString)
      if (cur != null) cur.whenExit()
      prev = cur
      cur= state
      changes.onNext(cur)
      current.whenEnter()
    }

    override def whenEnter(): Unit = enter()

    def enter() = goTo(start)

    // is this correct??
    override def onEvent: OnEvent = current.onEvent
  }

}