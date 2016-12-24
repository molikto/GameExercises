package org.snailya.bnw


import com.badlogic.gdx.{Game, Gdx, InputProcessor, Screen}
import com.badlogic.gdx.Gdx._
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.VertexAttributes._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.g3d._
import com.badlogic.gdx.graphics.g3d.attributes._
import com.badlogic.gdx.graphics.g3d.environment._
import com.badlogic.gdx.graphics.g3d.loader.{G3dModelLoader, ObjLoader}
import com.badlogic.gdx.graphics.g3d.utils._
import com.badlogic.gdx.math.{MathUtils, Matrix4, Vector3}
import com.badlogic.gdx.utils.{JsonReader, UBJsonReader}

/**
  * Created by molikto on 2016/22/12.
  */

class Bnw() extends GameWrapper(wrapper => new GameWrapperInner(wrapper) {


  /**
    *
    *
    * camera and resize
    *
    *
    */

  val camera = new PerspectiveCamera(38, 800, 480)
  //val camera = new OrthographicCamera(800, 480)
  camera.position.set(20f, 25f, 20f)
  camera.lookAt(0, 0, 0)
  camera.near = 1f
  camera.far = 300f
  camera.update()

  override def resize(width: Int, height: Int): Unit = {
    // when a window is resized, the frame buffer is resized, and we call glViewport to it, which determine the view buffer size?
    // then this method is called with LOGICAL width, height of the new window
    camera.viewportWidth = width
    camera.viewportHeight = height
    camera.update()
  }

  val UseCameraController = true

  val cameraController = new FirstPersonCameraController(camera)

  if (UseCameraController) input.setInputProcessor(cameraController)

  /**
    *
    *
    *
    * testing assets
    *
    *
    *
    */
  val xAxis = {
    val a = new ModelInstance(new ModelBuilder().createBox(100f, 0.1f, 0.1f,
      new Material(ColorAttribute.createDiffuse(Color.RED)), Usage.Position | Usage.Normal))
    a.transform.translate(50f, 0, 0)
    a
  }
  val yAxis = {
    val a = new ModelInstance(new ModelBuilder().createBox(0.1f, 100f, 0.1f,
      new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position | Usage.Normal))
    a.transform.translate(0, 50f, 0)
    a
  }
  val zAxis = {
    val a = new ModelInstance(new ModelBuilder().createBox(0.1f, 0.1f, 100f,
      new Material(ColorAttribute.createDiffuse(Color.BLUE)), Usage.Position | Usage.Normal))
    a.transform.translate(0, 0, 50f)
    a
  }

  val environment = new Environment()
  environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f))
  //environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))

  val shadowLight = new DirectionalShadowLight(2048, 2048, 120f, 120f, .1f, 50f).set(0.5f, 0.5f, 0.5f, 15.0f, -35f, 35f).asInstanceOf[DirectionalShadowLight]
  environment.add(shadowLight)
  environment.shadowMap = shadowLight

  val shadowBatch = new ModelBatch(new DepthShaderProvider())



  val cube = {
    val a = new ModelInstance(new ModelBuilder().createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position | Usage.Normal))
    a.transform.translate(10, 0, 0)
    //instance.calculateTransforms()
    a
  }

  val loader = new G3dModelLoader(new JsonReader())
  object model {
    val natures = (1 to 75).map(i => {
      loader.loadModel(files.internal("naturePack_" + ("000" + i.toString).takeRight(3) + ".g3dj"))
    })
  }

  val natures = model.natures.zipWithIndex.flatMap(pair => {
    val index = pair._2
    val sqrt = Math.sqrt(model.natures.size).toInt
    val ix = index % sqrt
    val iz = index / sqrt
    val size = 3f
    val matrix = new Matrix4().translate(size * ix, 0, size * iz)
    Seq(new ModelInstance(model.natures(0), matrix), new ModelInstance(pair._1, matrix))
  })

  /**
    *
    *
    *
    * more models
    *
    *
    */




  val modelBatch = new ModelBatch()


  override def render(): Unit = {
    /**
      * handle input first
      */


    if (!UseCameraController) {
      if (input.isKeyPressed(Keys.W)) {
        camera.translate(0, 0, -delta * 10)
      }
      if (input.isKeyPressed(Keys.S)) {
        camera.translate(0, 0, delta * 10)
      }
      if (input.isKeyPressed(Keys.A)) {
        camera.translate(-delta * 10, 0, 0)
      }
      if (input.isKeyPressed(Keys.D)) {
        camera.translate(delta * 10, 0, 0)
      }
      camera.update()
    } else {
      cameraController.update()
    }

    //create shadow texture
    shadowLight.begin(Vector3.Zero, camera.direction)
    shadowBatch.begin(shadowLight.getCamera)

    natures.foreach(a => shadowBatch.render(a))

    shadowBatch.end()
    shadowLight.end()

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
    gl.glClearColor(r(0x86), r(0x8a), r(0x78), 1)
    gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT)

    modelBatch.begin(camera)
    modelBatch.render(xAxis)
    modelBatch.render(yAxis)
    modelBatch.render(zAxis)

    //modelBatch.render(cube, environment)
    natures.foreach(a => modelBatch.render(a, environment))

    modelBatch.end()

  }


})
