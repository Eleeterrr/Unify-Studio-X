#version 330 core

layout(location = 0) in vec2 aPos;

out vec2 vTexCoord;

uniform mat4 uProjection;
uniform vec4 uRect;

void main()
{
    vTexCoord      = vec2(aPos.x, 1.0 - aPos.y);
    vec2 screenPos = uRect.xy + aPos * uRect.zw;
    gl_Position    = uProjection * vec4(screenPos, 0.0, 1.0);
}
