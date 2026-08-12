#version 330 core

out vec4 FragColor;

in float vHoverWeight;

uniform vec3 uOutlineColor;

void main()
{
    if (vHoverWeight < 0.01)
    {
        discard;
    }

    FragColor = vec4(uOutlineColor, 1.0);
}
