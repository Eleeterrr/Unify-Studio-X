#version 330 core

layout(location = 0) in vec3  aPos;
layout(location = 1) in vec4  aColor;
layout(location = 2) in vec2  aUV;
layout(location = 3) in float aWidth;

uniform mat4 uView;
uniform mat4 uProjection;

out vec4 vColor;
out vec2 vUV;

void main() {
    vec3 camRight = vec3(uView[0][0], uView[1][0], uView[2][0]);
    vec3 expanded = aPos + camRight * aWidth;
    gl_Position = uProjection * uView * vec4(expanded, 1.0);
    vColor = aColor;
    vUV = aUV;
}
