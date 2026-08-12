#version 330 core

in vec2 vTexCoord;

uniform sampler2D uImage;
uniform vec2 uTexelSize;
uniform bool uHorizontal;

out vec4 fragColor;

void main()
{
    vec2 off = uHorizontal ? vec2(uTexelSize.x, 0.0) : vec2(0.0, uTexelSize.y);
    vec3 result = texture(uImage, vTexCoord).rgb * 0.227027;
    result += texture(uImage, vTexCoord + off * 1.0).rgb * 0.1945946;
    result += texture(uImage, vTexCoord - off * 1.0).rgb * 0.1945946;
    result += texture(uImage, vTexCoord + off * 2.0).rgb * 0.1216216;
    result += texture(uImage, vTexCoord - off * 2.0).rgb * 0.1216216;
    result += texture(uImage, vTexCoord + off * 3.0).rgb * 0.054054;
    result += texture(uImage, vTexCoord - off * 3.0).rgb * 0.054054;
    result += texture(uImage, vTexCoord + off * 4.0).rgb * 0.016216;
    result += texture(uImage, vTexCoord - off * 4.0).rgb * 0.016216;
    fragColor = vec4(result, 1.0);
}
