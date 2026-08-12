#version 330 core

layout (location = 0) in vec2 aPosition;

uniform mat4  u_projection;
uniform mat4  u_view;
uniform vec3  u_sunDir;
uniform float u_size;

out vec2 vTexCoord;

void main()
{
    vTexCoord = aPosition;

    mat4 viewRotation = mat4(mat3(u_view));
    
    vec4 sunViewPos = viewRotation * vec4(-u_sunDir, 0.0);
    
    vec3 pos = normalize(sunViewPos.xyz) * 100.0;
    
    pos.xy += aPosition * u_size;
    
    gl_Position = u_projection * vec4(pos, 1.0);
    
    gl_Position.z = gl_Position.w;
}
