#ifdef __APPLE__
#define __gl3_h_
#define GL_DO_NOT_WARN_IF_MULTI_GL_VERSION_HEADERS_INCLUDED
#endif

#include "GL/glew.h"
#define GLFW_INCLUDE_GLCOREARB
#include "GLFW/glfw3.h"
#include "common.h"
#include "game.h"



int main() {


  // TODO get these parameters
  bool fullScreen = false;
  int windowWidth = 1024;
  int windowHeight = 768;

  if (!glfwInit()) {
    err("glfwInit()");
    exit(EXIT_FAILURE);
  }


  // TODO window size, icon, full screen support
  GLFWwindow *window;
  {
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
    // TODO test out
    glfwWindowHint(GLFW_SAMPLES, 4);
    // TODO is this version ok on other machines?
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

    window = glfwCreateWindow(windowWidth, windowHeight, "Bluebear", fullScreen ? glfwGetPrimaryMonitor() : nullptr, nullptr);
    if (!window) {
      err("glfwCreateWindow()");
      glfwTerminate();
      exit(1);
    }
    glfwMakeContextCurrent(window);
    glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);
    // TODO what does this means?
    glfwSwapInterval(0);
  }

  {
    glewExperimental = 1;
    if (GLEW_OK != glewInit()) {
      err("glewInit()");
      exit(1);
    }
    glGetError();
  }

  //glViewport(0, 0, windowWidth, windowHeight);
  // glEnable(GL_DEPTH_TEST);
  // mat4 projection = perspective(radians(60.0f), windowWidth / (float) windowHeight, 0.5f, 150.f);
  glEnable(GL_BLEND);
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);



  Graphics graphics = Graphics();
  Game game = Game();

  double time = glfwGetTime();
  int sec = (int) time;
  int frameCount = 0;
  int frameRate = 0;
  while (!glfwWindowShouldClose(window)) {
    if (glfwGetKey(window, GLFW_KEY_ESCAPE)) {
      glfwSetWindowShouldClose(window, GL_TRUE);
      break;
    }

    double current = glfwGetTime();
    double dt = current - time;
    time = current;

    int curSec = (int) current;
    if (curSec != sec) {
      sec = curSec;
      frameRate = frameCount;
      frameCount = 0;
      glfwSetWindowTitle(window, to_string(frameRate).c_str());
    } else {
      frameCount++;
    }

    glClear(GL_COLOR_BUFFER_BIT);

    game.updateAndRender(graphics, *window, time, 0);

    glfwSwapBuffers(window);
    glfwPollEvents();
  }

  glfwDestroyWindow(window);
  glfwTerminate();
  return 0;
}



