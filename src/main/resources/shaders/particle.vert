#version 330 core

layout (location = 0) in vec2 aPosition;

layout (location = 1) in vec3 aInstPosition;
layout (location = 2) in float aInstSize;
layout (location = 3) in vec4 aInstColor;
layout (location = 4) in float aInstRotation;
layout (location = 5) in vec4 aInstUV; // u0, v0, u1, v1
layout (location = 6) in float aInstPadding;

uniform mat4 uView;
uniform mat4 uProjection;
uniform vec3 uCameraRight;
uniform vec3 uCameraUp;

out vec4 vColor;
out vec2 vTexCoord;

void main()
{
    vColor = aInstColor;
    
    float u = mix(aInstUV.x, aInstUV.z, aPosition.x + 0.5);
    float v = mix(aInstUV.y, aInstUV.w, aPosition.y + 0.5);
    vTexCoord = vec2(u, v);
    
    vec3 localPos = vec3(aPosition, 0.0) * aInstSize;
    
    float cosRot = cos(aInstRotation);
    float sinRot = sin(aInstRotation);
    
    float rotX = localPos.x * cosRot - localPos.y * sinRot;
    float rotY = localPos.x * sinRot + localPos.y * cosRot;
    
    vec3 worldPos = aInstPosition+ uCameraRight * rotX+ uCameraUp * rotY;
                  
    gl_Position = uProjection * uView * vec4(worldPos, 1.0);
}
