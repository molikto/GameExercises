#pragma once

#include "common.h"
#include "bitmap.h"

class Texture {
public:
  Texture(const Bitmap& bitmap, int minMagFiler = GL_LINEAR, int wrapMode = GL_CLAMP_TO_EDGE);
  ~Texture();
  uint width;
  uint height;
  uint object;
};

