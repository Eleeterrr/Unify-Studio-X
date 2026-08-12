#version 330 core

layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aUV;

layout(location = 2) in vec3  iPosition;
layout(location = 3) in vec4  iColor;
layout(location = 4) in vec2  iSize;
layout(location = 5) in float iRotation;
layout(location = 6) in vec4  iUVRegion;

uniform mat4 uView;
uniform mat4 uProjection;
uniform int  uBillboard;

out vec4 vColor;
out vec2 vUV;

void main() {
    float c = cos(iRotation);
    float s = sin(iRotation);
    vec2 rotated = vec2(aPos.x * c - aPos.y * s,
                        aPos.x * s + aPos.y * c);
    rotated *= iSize;

    vec3 worldPos = iPosition;

    if (uBillboard == 0) {

        vec3 right = vec3(uView[0][0], uView[1][0], uView[2][0]);
        vec3 up    = vec3(uView[0][1], uView[1][1], uView[2][1]);
        worldPos += right * rotated.x + up * rotated.y;
    } else if (uBillboard == 1) {

        worldPos.x += rotated.x;
        worldPos.y += rotated.y;
    } else {

        worldPos.x += rotated.x;
        worldPos.y += rotated.y;
    }

    gl_Position = uProjection * uView * vec4(worldPos, 1.0);

    vUV    = mix(iUVRegion.xy, iUVRegion.zw, aUV);
    vColor = iColor;
}
