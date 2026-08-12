#version 330 core

in vec2 vTexCoord;

uniform vec3  u_sunColor;
uniform float u_intensity;

out vec4 fragColor;

void main()
{
    float dist = length(vTexCoord);
    
    float disk = smoothstep(0.1, 0.05, dist);
    
    float glow = exp(-dist * 4.0) * 0.5;
    float glare = exp(-dist * 15.0) * 0.8;
    
    float alpha = disk + glow + glare;
    
    vec3 color = u_sunColor * alpha * u_intensity;
    
    color += u_sunColor * disk * u_intensity * 0.5;

    fragColor = vec4(color, alpha * 1.5);
}
