
#include "bitmap.h"
#define STBI_FAILURE_USERMSG
#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"

Bitmap* Bitmap::fromFile(string fn) {
  int w, h, channels;
  unsigned char* pixels = stbi_load(fn.c_str(), &w, &h, &channels, 0);
  if (!pixels) {
    err("load bitmap from file");
    exit(1);
  }
  return new Bitmap(w, h, (Format) channels, pixels);
}

Bitmap::Bitmap(uint width, uint height, Bitmap::Format format, unsigned char *pixels):
    width(width), height(height), format(format), pixels(pixels) {
}

Bitmap::~Bitmap() {
  if(pixels) free(pixels);
}

inline unsigned pixelOffset(unsigned col, unsigned row, unsigned width, unsigned height, Bitmap::Format format) {
  return (row*width + col)*format;
}


 void Bitmap::flipVertically() {
   unsigned long rowSize = format*width;
   unsigned char* rowBuffer = new unsigned char[rowSize];
   unsigned halfRows = height / 2;

   for(unsigned rowIdx = 0; rowIdx < halfRows; ++rowIdx){
     unsigned char* row = pixels + pixelOffset(0, rowIdx, width, height, format);
     unsigned char* oppositeRow = pixels + pixelOffset(0, height - rowIdx - 1, width, height, format);

     memcpy(rowBuffer, row, rowSize);
     memcpy(row, oppositeRow, rowSize);
     memcpy(oppositeRow, rowBuffer, rowSize);
   }

   delete rowBuffer;

}
