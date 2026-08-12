#version 330 core

in vec4 vColor;
in vec2 vTexCoord;

uniform sampler2D uTexture;

out vec4 fragColor;

void main()
{
    vec4 texColor = texture(uTexture, vTexCoord);
    float heat = texColor.a * vColor.a * max(max(vColor.r, vColor.g), vColor.b);
    if (heat < 0.01)
    {
        discard;
    }
    vec2 flow = normalize(vColor.rg + vec2(0.001)) * heat;
    fragColor = vec4(flow * 0.5 + 0.5, heat, 1.0);
}
