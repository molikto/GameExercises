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
  val camera = new OrthographicCamera()
  camera.position.set(30f, 30f, 30f)
  camera.lookAt(0, 0, 0)
  camera.update()

  override def resize(width: Int, height: Int): Unit = {
    // when a window is resized, the frame buffer is resized, and we call glViewport to it, which determine the view buffer size?
    // then this method is called with LOGICAL width, height of the new window
    camera.viewportWidth = width / 32
    camera.viewportHeight = height / 32
    camera.update()
  }

  val USE_CAMERA_CONTROLLER = false

  val cameraController = new FirstPersonCameraController(camera)

  if (USE_CAMERA_CONTROLLER) input.setInputProcessor(cameraController)

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

    val MEDIEVALS = Seq("Banner_01.g3dj", "Castle_Wall_01.g3dj", "Grey_Arch_01.g3dj", "Grey_Border_Wall_01.g3dj", "Grey_Broken_Wall_01.g3dj", "Grey_Corner_01.g3dj", "Grey_Door_Round_01.g3dj", "Grey_Door_Square_01.g3dj", "Grey_Pole_01.g3dj", "Grey_Short_Wall_01.g3dj", "Grey_Slanted_Pole_01.g3dj", "Grey_Slanted_Wall_01.g3dj", "Grey_Small_Wall_01.g3dj", "Grey_Triangle_01.g3dj", "Grey_Wall_01.g3dj", "Grey_Window_Narrow_01.g3dj", "Grey_Window_Round_01.g3dj", "Grey_Window_Round_Long_01.g3dj", "Grey_Window_Round_Sill_01.g3dj", "Grey_Window_Square_01.g3dj", "Grey_Window_Square_Sill_01.g3dj", "Iron_Door_01.g3dj", "Lightpost_01.g3dj", "Plate_Corner_01.g3dj", "Plate_Curve_01.g3dj", "Plate_Pavement_01.g3dj", "Plate_Road_01.g3dj", "Plate_Sidewalk_01.g3dj", "Plate_Wood_01.g3dj", "Roof_Corner_Green_01.g3dj", "Roof_Corner_Red_01.g3dj", "Roof_Inner_Corner_Green_01.g3dj", "Roof_Inner_Corner_Red_01.g3dj", "Roof_Point_Green_01.g3dj", "Roof_Point_Red_01.g3dj", "Roof_Slant_Green_01.g3dj", "Roof_Slant_Red_01.g3dj", "Roof_Straight_Green_01.g3dj", "Roof_Straight_Red_01.g3dj", "Shield_Green_01.g3dj", "Shield_Red_01.g3dj", "Stairs_Stone_01.g3dj", "Stairs_Wood_01.g3dj", "Tree_01.g3dj", "Wood_Arch_01.g3dj", "Wood_Border_Wall_01.g3dj", "Wood_Broken_Wall_01.g3dj", "Wood_Corner_01.g3dj", "Wood_Door_01.g3dj", "Wood_Door_Round_01.g3dj", "Wood_Door_Square_01.g3dj", "Wood_Pole_01.g3dj", "Wood_Railing_01.g3dj", "Wood_Slanted_Pole_01.g3dj", "Wood_Slanted_Wall_01.g3dj", "Wood_Small_Wall_01.g3dj", "Wood_Tiny_Wall_01.g3dj", "Wood_Triangle_01.g3dj", "Wood_Wall_01.g3dj", "Wood_Wall_Cross_01.g3dj", "Wood_Wall_Double_Cross_01.g3dj", "Wood_Window_Narrow_01.g3dj", "Wood_Window_Round_01.g3dj", "Wood_Window_Round_Long_01.g3dj", "Wood_Window_Round_Sill_01.g3dj", "Wood_Window_Square_01.g3dj", "Wood_Window_Square_Sill_01.g3dj")
    val medievals = MEDIEVALS
      .map(f => loader.loadModel(files.internal(f)))
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

  val medievals = model.medievals.zipWithIndex.flatMap(pair => {
    val index = pair._2
    val sqrt = Math.sqrt(model.medievals.size).toInt
    val ix = index % sqrt
    val iz = index / sqrt
    val size = 3f
    val matrixg = new Matrix4().translate(size * ix, -0.25f, size * iz)
    val matrix = new Matrix4().translate(size * ix, 0, size * iz)
    Seq(new ModelInstance(model.medievals(model.MEDIEVALS.indexOf("Plate_Pavement_01.g3dj")), matrixg), new ModelInstance(pair._1, matrix))
  })

  /**
    *
    *
    *
    * more models
    *
    *
    */

  val environment = new Environment()
  environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.7f, 0.65f, 0.7f))
  //environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))

  val shadowLight = new DirectionalShadowLight(2048, 2048, 120f, 120f, .1f, 50f).set(0.3f, 0.3f, 0.3f, 5.0f, -35f, 15f).asInstanceOf[DirectionalShadowLight]
  environment.add(shadowLight)
  environment.shadowMap = shadowLight

  val shadowBatch = new ModelBatch(new DepthShaderProvider())

  val modelBatch = new ModelBatch()




  override def render(): Unit = {
    /**
      * handle input first
      */

    if (!USE_CAMERA_CONTROLLER) {
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

    medievals.foreach(a => shadowBatch.render(a))

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
    gl.glClearColor(r(0x35), r(0x4a), r(0x5f), 1)
    gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT)

    modelBatch.begin(camera)
    modelBatch.render(xAxis)
    modelBatch.render(yAxis)
    modelBatch.render(zAxis)

    //modelBatch.render(cube, environment)
    medievals.foreach(a => modelBatch.render(a, environment))

    modelBatch.end()

  }


})
