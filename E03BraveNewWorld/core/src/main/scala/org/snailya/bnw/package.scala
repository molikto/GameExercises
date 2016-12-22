package org.snailya


import com.badlogic.gdx.{Game, Gdx, Screen}
import com.badlogic.gdx.Gdx._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.math.{MathUtils, Vector3}

package object bnw {

  def vector3(x: Float, y: Float, z: Float) = new Vector3(x, y, z)

  class GameWrapperInner(val wrapper: GameWrapper) {
    def screen_=(s: Screen) = wrapper.setScreen(s)
    def screen: Screen = wrapper.getScreen

    def resize(width: Int, height: Int): Unit = {}
    def render(): Unit = {}
  }

  class GameWrapper(val inner: GameWrapper => GameWrapperInner) extends Game {

    // println(System.getProperty("user.dir"))


    var bnw: GameWrapperInner = null
    override def create(): Unit = {
      bnw = inner(this)
    }



    override def resize(width: Int, height: Int): Unit = bnw.resize(width, height)

    override def render(): Unit = bnw.render()



  }
}

