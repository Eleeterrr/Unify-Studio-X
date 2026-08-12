#version 330 core

in vec2 vTexCoord;

uniform sampler2D uSceneColor;
uniform sampler2D uDistortionMap;
uniform float uDistortionStrength;

out vec4 fragColor;

void main()
{
    vec2 distortion = texture(uDistortionMap, vTexCoord).rg * 2.0 - 1.0;
    float mask = texture(uDistortionMap, vTexCoord).b;
    vec2 offset = distortion * uDistortionStrength * mask;
    vec3 color = texture(uSceneColor, vTexCoord + offset).rgb;
    fragColor = vec4(color, 1.0);
}
