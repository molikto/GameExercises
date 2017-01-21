#pragma once

#include<string>
#include<fstream>
#include<iostream>
#include<glm/glm.hpp>
#include<glm/gtc/matrix_transform.hpp>
#include<glm/gtx/transform2.hpp>
#include "GL/glew.h"




using namespace std;
using namespace glm;

void err(string s);
string fileRead(string name);
void glCheckError(string str);


/* primitive types used


int
uint


 */
