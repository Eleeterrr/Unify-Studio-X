#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexCoord;

out vec4 vColor;
out vec2 vTexCoord;

uniform mat4 uProjection;

void main()
{
    vColor = aColor;
    vTexCoord = aTexCoord;
    gl_Position = uProjection * vec4(aPos, 1.0);
}
