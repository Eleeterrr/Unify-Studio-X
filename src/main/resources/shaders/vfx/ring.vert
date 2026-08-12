#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aUV;

layout(location = 2) in vec3  iCenter;
layout(location = 3) in vec4  iColor;
layout(location = 4) in float iRadius;
layout(location = 5) in float iThickness;
layout(location = 6) in float iRotation;

uniform mat4 uView;
uniform mat4 uProjection;

out vec4 vColor;
out vec2 vUV;

void main() {
    float c = cos(iRotation);
    float s = sin(iRotation);

    vec3 rotated = vec3(
        aPos.x * c - aPos.z * s,
        aPos.y,
        aPos.x * s + aPos.z * c
    );

    vec3 world = iCenter + rotated * iRadius;
    gl_Position = uProjection * uView * vec4(world, 1.0);
    vColor = iColor;
    vUV    = aUV;
}
