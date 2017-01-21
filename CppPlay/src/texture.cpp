#include "texture.h"


static uint textureFormatForBitmapFormat(Bitmap::Format format)
{
  switch (format) {
    case Bitmap::G: return GL_LUMINANCE;
    case Bitmap::GA: return GL_LUMINANCE_ALPHA;
    case Bitmap::RGB: return GL_RGB;
    case Bitmap::RGBA: return GL_RGBA;
    default: throw std::runtime_error("Unrecognised Bitmap::Format");
  }
}



Texture::Texture(const Bitmap &bitmap, int minMagFiler, int wrapMode)
  : width(bitmap.width), height(bitmap.height) {
  glGenTextures(1, &object);
  glBindTexture(GL_TEXTURE_2D, object);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minMagFiler);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, minMagFiler);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);
  glTexImage2D(GL_TEXTURE_2D,
      0,
      textureFormatForBitmapFormat(bitmap.format),
      (GLsizei)bitmap.width,
      (GLsizei)bitmap.height,
      0,
      textureFormatForBitmapFormat(bitmap.format),
      GL_UNSIGNED_BYTE,
      bitmap.pixels);
  glBindTexture(GL_TEXTURE_2D, 0);
  glCheckError("Texture::Texture");
}

Texture::~Texture() {
  glDeleteTextures(1, &object);
}
