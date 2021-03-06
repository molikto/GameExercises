#version 400

layout(location = 0) in vec4 position;
layout(location = 1) in vec2 fragTexCoord;
out vec2 texCoord;

void main()
{
    gl_Position = position;
    texCoord = fragTexCoord;
}