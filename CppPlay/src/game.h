#pragma once

#include "graphics.h"
#include "GLFW/glfw3.h"

class Game {
public:
  Game();
  void updateAndRender(Graphics &graphics, GLFWwindow &window, double time, double dt);
};
