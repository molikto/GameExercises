package org.snailya.exercise01

import com.badlogic.gdx.backends.lwjgl._

object Main extends App {
    val cfg = new LwjglApplicationConfiguration
    cfg.title = "Exercise01"
    cfg.height = 640
    cfg.width = 640
    cfg.forceExit = false
    new LwjglApplication(new Cmc, cfg)
}
