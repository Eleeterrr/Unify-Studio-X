#version 330 core

in vec2 vTexCoord;

uniform sampler2D uSceneColor;
uniform float uBloomThreshold;

out vec4 fragColor;

void main()
{
    vec3 color = texture(uSceneColor, vTexCoord).rgb;
    float brightness = max(max(color.r, color.g), color.b);
    float contribution = max(brightness - uBloomThreshold, 0.0);
    fragColor = vec4(color * contribution, 1.0);
}
