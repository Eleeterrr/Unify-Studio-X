#version 330 core

in  vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D uMsdfAtlas;
uniform vec4      uTextColor;
uniform float     uPxRange;

float median(float r, float g, float b)
{
    return max(min(r, g), min(max(r, g), b));
}

float getScreenPxRange()
{
    vec2 texSize       = vec2(textureSize(uMsdfAtlas, 0));
    vec2 unitRange     = vec2(uPxRange) / texSize;
    vec2 screenTexSize = vec2(1.0) / fwidth(TexCoord);
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

float sampleMsdf(vec2 uv)
{
    vec3 msd = texture(uMsdfAtlas, uv).rgb;
    return median(msd.r, msd.g, msd.b);
}

void main()
{
    float screenPxRange = getScreenPxRange();

    float opacity;

    if (screenPxRange < 2.5)
    {
        vec2 dx = dFdx(TexCoord) * 0.25;
        vec2 dy = dFdy(TexCoord) * 0.25;

        float s0 = sampleMsdf(TexCoord + dx + dy);
        float s1 = sampleMsdf(TexCoord - dx + dy);
        float s2 = sampleMsdf(TexCoord + dx - dy);
        float s3 = sampleMsdf(TexCoord - dx - dy);

        opacity = (clamp(screenPxRange * (s0 - 0.5) + 0.5, 0.0, 1.0) +
        clamp(screenPxRange * (s1 - 0.5) + 0.5, 0.0, 1.0) +
        clamp(screenPxRange * (s2 - 0.5) + 0.5, 0.0, 1.0) +
        clamp(screenPxRange * (s3 - 0.5) + 0.5, 0.0, 1.0)) * 0.25;
    } else
    {
        float sd = sampleMsdf(TexCoord);
        opacity = clamp(screenPxRange * (sd - 0.5) + 0.5, 0.0, 1.0);
    }

    if (opacity < 0.001) discard;
    FragColor = vec4(uTextColor.rgb, uTextColor.a * opacity);
}
