#version 330 core

in vec2 vTexCoord;

uniform sampler2D uSceneColor;
uniform sampler2D uBloomTexture;
uniform float uBloomStrength;

out vec4 fragColor;

void main()
{
    vec3 scene = texture(uSceneColor, vTexCoord).rgb;
    vec3 bloom = texture(uBloomTexture, vTexCoord).rgb;
    vec3 combined = scene + bloom * uBloomStrength;
    combined = combined / (combined + vec3(1.0));
    fragColor = vec4(pow(combined, vec3(1.0 / 2.2)), 1.0);
}
