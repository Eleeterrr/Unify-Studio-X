#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;

out vec2 TexCoord;

uniform mat4 uProjection;
uniform vec2 uOffset;
uniform float uScale;

void main()
{
    float sx = aPos.x * uScale;
    float sy = -aPos.y * uScale;
    gl_Position = uProjection * vec4(sx + uOffset.x, sy + uOffset.y, 0.0, 1.0);
    TexCoord = aTexCoord;
}
