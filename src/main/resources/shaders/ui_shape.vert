#version 330 core

layout(location = 0) in vec2 aPos;

uniform mat4 uProjection;

void main()
{
    vec4 clip = uProjection * vec4(aPos, 0.0, 1.0);
    gl_Position = clip;
}
