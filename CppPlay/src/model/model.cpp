#include "model.h"


Model::Model(Shader &shader, uint vert_count, float *vert_attrs)
  :shader(shader), vert_count(vert_count) {
  glGenVertexArrays(1, &vao);
  glBindVertexArray(vao);


  glGenBuffers(1, &vbo);
  glBindBuffer(GL_ARRAY_BUFFER, vbo);
  glBufferData(GL_ARRAY_BUFFER,
      shader.attrs_length * vert_count,
      vert_attrs, GL_STATIC_DRAW);
  uint padding = 0;
  uint total = shader.attrs_length;
  for (uint i = 0; i < shader.attrs_count; i++) {
    int dp = shader.attrs[i]->length;
    shader.attrs[i]->bind(i, total, (void const *) padding);
    padding += dp;
  }

  glBindVertexArray(0);

  glCheckError("model init");
}

void Model::render() {

  // mat4 view = lookAt(vec3(0.0f, 0.0f, 3.0f), vec3(0.0f, 0.0f, 0.0f), vec3(0.0f, 1.0f, 0.0f));

  glUseProgram(shader.program);
  glCheckError("Model::useProgram");
  for (uint i = 0; i < shader.uniforms_count; i++) {
    shader.uniforms[i]->prerender(shader);
  }
  glCheckError("Model::prerender");
  glBindVertexArray(vao);
  glDrawArrays(GL_TRIANGLES, 0, vert_count);
  glBindVertexArray(0);
  glCheckError("Model::render");
  for (uint i = 0; i < shader.uniforms_count; i++) {
    shader.uniforms[i]->postrender();
  }
  glCheckError("Model::postrender");
  glUseProgram(0);
}
