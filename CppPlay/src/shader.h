#pragma once

#include "common.h"
#include "texture.h"
#include "shader.h"


class Shader;

class AttrBinding {
public:
  uint length;
  AttrBinding(uint l)
      :length((uint) (l * sizeof(float))) {};
  void bind(uint position, uint length, const void *padding);
};


class UniformBinding {
public:
  UniformBinding(Texture& texture, string name, uint glTexture = GL_TEXTURE0, float v = 0)
      : texture(texture) , glTexture(glTexture), v(v), name(name) {};
  Texture &texture;
  uint glTexture;
  string name;
  int v;
  void prerender(Shader &shader);
  void postrender();
};


class Shader {
public:
  Shader(string folder, uint attrs_count, AttrBinding **attrs, uint uniforms_count, UniformBinding **uniforms);
  uint vert;
  uint frag;
  uint program;
  uint attrs_count;
  AttrBinding **attrs;
  uint attrs_length;
  uint uniforms_count;
  UniformBinding **uniforms;
  ~Shader();
};
