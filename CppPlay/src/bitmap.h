#pragma once


#include "common.h"


class Bitmap {


public:
  enum Format {
    G = 1,
    GA = 2,
    RGB = 3,
    RGBA = 4
  };
  Bitmap(uint width, uint height, Format format, unsigned char *pixels);
  ~Bitmap();

  static Bitmap * fromFile(string fn);

  uint width;
  uint height;
  Format format;
  unsigned char* pixels;
  void flipVertically();
};
