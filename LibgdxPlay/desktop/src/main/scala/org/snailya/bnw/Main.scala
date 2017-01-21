package org.snailya.bnw

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.HdpiMode
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}

/**
  * Created by molikto on 2016/22/12.
  */
object Main extends App {
    val cfg = new Lwjgl3ApplicationConfiguration
    cfg.setTitle("Brave New World")
    cfg.setResizable(true)
    cfg.setWindowedMode(800, 480)
    cfg.setIdleFPS(60)
    //cfg.useOpenGL3(true, 3, 2)
    new Lwjgl3Application(new Bnw, cfg)
    System.exit(0)
}
