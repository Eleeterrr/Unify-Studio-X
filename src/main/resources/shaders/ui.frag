#version 330 core

in vec2 vTexCoord;

uniform vec4 uColor;
uniform sampler2D uSampler;
uniform int uHasTexture;

out vec4 fragColor;

void main()
{
    if (uHasTexture != 0)
    {
        fragColor = texture(uSampler, vTexCoord) * uColor;
    } else {
        fragColor = uColor;
    }
}
