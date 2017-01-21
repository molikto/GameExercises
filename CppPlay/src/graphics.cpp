#include "graphics.h"




float colorTriangle_pos[] =  {
    0.75f, 0.75f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
    0.75f, -0.75f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
    -0.75f, -0.75f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f
};


float texturedTriangle_pos[] =  {
    0.0f, 0.8f, 0.0f, 1.0f, 0.5f, 1.0f,
    -0.8f,-0.8f, 0.0f, 1.0f, 0.0f, 0.0f,
    0.8f,-0.8f, 0.0f, 1.0f, 1.0f, 0.0f,
};

Model *debug_colorTriangle() {

  auto attr1 = new AttrBinding(4);
  auto attr2 = new AttrBinding(4);
  auto attrs = new AttrBinding*[2];
  attrs[0] = attr1;
  attrs[1] = attr2;
  Shader *color_triangle = new Shader("colored_triangle", 2, attrs, 0, nullptr);
  return new Model(*color_triangle, 3, colorTriangle_pos);
}

Model *debug_texturedTriangle() {
  auto attr1 = new AttrBinding(4);
  auto attr2 = new AttrBinding(2);
  auto attrs = new AttrBinding*[2];
  attrs[0] = attr1;
  attrs[1] = attr2;
  auto bitmap = Bitmap::fromFile("../res/images/hazard.png");
  bitmap->flipVertically();
  auto texture = new Texture(*bitmap);
  auto uniform = new UniformBinding(*texture, "tex");
  auto uniforms = new UniformBinding*[1];
  uniforms[0] = uniform;
  Shader *textured_triangle = new Shader("textured_triangle", 2, attrs, 1, uniforms);
  return new Model(*textured_triangle, 3, texturedTriangle_pos);
}


Graphics::Graphics() {
  debug = debug_texturedTriangle();
}

void
Graphics::drawDebug() {
  debug->render();
}

