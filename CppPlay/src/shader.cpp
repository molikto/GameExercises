#include <stb.h>
#include "shader.h"



Shader::
Shader(string folder, uint attrs_count, AttrBinding** attrs, uint uniforms_count, UniformBinding** uniforms)
  :attrs_count(attrs_count), attrs(attrs), uniforms_count(uniforms_count), uniforms(uniforms)
{

  attrs_length = 0;
  for (int i = 0; i < attrs_count; i++) {
    attrs_length += attrs[i]->length;
  }

  string vertSrc = fileRead("../res/shaders/" + folder + "/shader.vert");
  vert = glCreateShader(GL_VERTEX_SHADER);
  const char *vertSrc_c = vertSrc.c_str();
  glShaderSource(vert, 1, &vertSrc_c, NULL);
  glCompileShader(vert);


  string fragSrc = fileRead("../res/shaders/" + folder + "/shader.frag");
  const char *fragSrc_c = fragSrc.c_str();
  frag = glCreateShader(GL_FRAGMENT_SHADER);
  glShaderSource(frag, 1, &fragSrc_c, NULL);
  glCompileShader(frag);

//  auto shaderCompiled = [](GLuint shader) {
//    GLint compileSuccess;
//    glGetShaderiv(shader, GL_COMPILE_STATUS, &compileSuccess);
//
//    if (!compileSuccess) {
//      char buffer[512];
//      glGetShaderInfoLog(shader, 512, NULL, buffer);
//      cerr << "compile shader " << compileSuccess << "\n" << buffer << endl;
//      glDeleteShader(shader);
//      exit(EXIT_FAILURE);
//    }
//  };
//
//  shaderCompiled(vert);
//  shaderCompiled(frag);

  program = glCreateProgram();
  glAttachShader(program, vert);
  glAttachShader(program, frag);

  glLinkProgram(program);
//  GLint linkSuccess;
//  glGetProgramiv(program, GL_LINK_STATUS, &linkSuccess);
//
//  if (!linkSuccess) {
//    cerr << "Error: program failed to link correctly." << endl;
//    glDeleteProgram(program);
//    exit(EXIT_FAILURE);
//  }
  glCheckError("shader compile");
};


Shader::
~Shader()
{
  if(vert) {
    glDetachShader(program, vert);
    glDeleteShader(vert);
  }
  if(frag) {
    glDetachShader(program, frag);
    glDeleteShader(frag);
  }
  if(program) {
    glDeleteShader(program);
  }
}



void
AttrBinding::bind(uint position, uint length, const void *padding) {
  glVertexAttribPointer(position, 4, GL_FLOAT, GL_FALSE, length, padding);
  glEnableVertexAttribArray(position);
};



void
UniformBinding::prerender(Shader &shader) {
  glActiveTexture(glTexture);
  glBindTexture(GL_TEXTURE_2D, texture.object);
  int unifrom = glGetUniformLocation(shader.program, name.c_str());
  glUniform1i(unifrom, v);
};
void
UniformBinding::postrender() {
  glBindTexture(GL_TEXTURE_2D, 0);
};
