#version 330 core

layout (location = 0) in vec3 aPosition;

uniform mat4 uProjection;
uniform mat4 uView;

out vec3 vLocalPos;

void main()
{
    vLocalPos = aPosition;
    
    mat4 viewRotationOnly = mat4(mat3(uView));
    vec4 pos = uProjection * viewRotationOnly * vec4(aPosition, 1.0);
    
    gl_Position = pos.xyww;
}
