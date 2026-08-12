#version 330 core

layout (location = 0) in vec3  aPos;
layout (location = 1) in vec3  aColor;
layout (location = 2) in float aAxisId;

out vec3  vColor;
out float vAxisId;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

void main()
{
    vColor  = aColor;
    vAxisId = aAxisId;
    gl_Position = uProjection * uView * uModel * vec4(aPos, 1.0);
}
