#pragma once

#include "../shader.h"

class Model {
public:
  Model(Shader &shader, uint vert_count, float *vert_attrs);
  void render();
  uint vbo;
  uint vao;
  Shader& shader;
  uint vert_count;
};
