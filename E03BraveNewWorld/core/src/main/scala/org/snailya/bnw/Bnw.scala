package org.snailya.bnw


import com.badlogic.gdx.{Game, Gdx, Screen}
import com.badlogic.gdx.Gdx._
import com.badlogic.gdx.graphics.VertexAttributes._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.g3d._
import com.badlogic.gdx.graphics.g3d.attributes._
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.{MathUtils, Vector3}

/**
  * Created by molikto on 2016/22/12.
  */

class Bnw() extends GameWrapper(wrapper => new GameWrapperInner(wrapper) {


  val camera = new PerspectiveCamera(67, 800, 480)

  val modelBatch = new ModelBatch()

  camera.position.set(10f, 10f, 10f)
  camera.lookAt(0, 0, 0)
  camera.near = 1f
  camera.far = 300f
  camera.update()

  val modelBuilder = new ModelBuilder()
  val model = modelBuilder.createBox(5f, 5f, 5f,
    new Material(ColorAttribute.createDiffuse(Color.GREEN)),
    Usage.Position | Usage.Normal)
  val instance = new ModelInstance(model)

  override def resize(width: Int, height: Int): Unit = {
  }


  override def render(): Unit = {
    /**
      * the window size is logical size.
      *
      * for example, opening a window of 800*480, will result in a window having 1600*960 pixels
      * but the window size is still 800*480
      *
      * frame buffer size is usually a multiply of the window size
      *
      * we most of time don't care about frame buffer size
      */
    gl.glClearColor(0.4f + MathUtils.random()*0.2f,0.4f + MathUtils.random()*0.2f,0.4f + MathUtils.random()*0.2f,1f)
    gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT)

    modelBatch.begin(camera)
    modelBatch.render(instance)
    modelBatch.end()

  }


})
