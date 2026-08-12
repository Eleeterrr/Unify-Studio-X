#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform vec4  uColor;
uniform vec2  uSize;
uniform float uRadius;

float roundedBoxSDF(vec2 CenterPosition, vec2 Size, float Radius)
{
    return length(max(abs(CenterPosition)-Size+Radius,0.0))-Radius;
}

void main()
{
    vec2 p = (TexCoord * uSize) - (uSize * 0.5);
    vec2 b = uSize * 0.5;

    float sd = roundedBoxSDF(p, b, uRadius);
    
    float edgeSoftness = max(fwidth(sd), 0.5);
    
    float opacity = 1.0 - smoothstep(0.0, edgeSoftness, sd);

    if (opacity < 0.001)
    {
        discard;
    }

    FragColor = vec4(uColor.rgb, uColor.a * opacity);
}
