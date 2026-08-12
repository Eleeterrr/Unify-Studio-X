#version 330 core

in vec4 vColor;
in vec2 vTexCoord;

out vec4 FragColor;

uniform int uHasTexture;
uniform sampler2D uSampler;

void main()
{
    if (uHasTexture != 0)
    {
        FragColor = texture(uSampler, vTexCoord) * vColor;
    } else {
        FragColor = vColor;
    }
}
