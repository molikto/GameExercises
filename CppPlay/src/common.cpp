#include "common.h"


void
err(string msg) {
  cerr << msg << endl;
}


string
fileRead(string name) {
  ifstream fin(name.c_str());
  string line = "";
  string str = "";
  while (getline(fin, line)) {
    str = str + "\n" + line;
  }
  fin.close();
  return str;
}

void
glCheckError(string str) {
  auto err = glGetError();
  if (err != GL_NO_ERROR) {
    cerr << str << ": " << err << endl;
    exit(-1);
  }
}
