#version 330 core

layout(location = 0) in vec2 aPos;

uniform mat4 uProjection;
uniform vec4 uRect;

out vec2 TexCoord;

void main()
{
    vec2 screenPos = uRect.xy + aPos * uRect.zw;
    gl_Position    = uProjection * vec4(screenPos, 0.0, 1.0);
    TexCoord       = aPos;
}
