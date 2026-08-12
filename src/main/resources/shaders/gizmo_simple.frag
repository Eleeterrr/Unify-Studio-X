#version 330 core

in vec3  vColor;
in float vAxisId;

out vec4 FragColor;

uniform int uHighlightAxis;
uniform int uActiveAxis;

void main()
{
    vec3 color = vColor;
    int  id    = int(round(vAxisId));

    if (id == uHighlightAxis)
    color = mix(color, vec3(1.0), 0.50);

    if (uActiveAxis > 0 && id == uActiveAxis)
    color = mix(color, vec3(1.0, 0.85, 0.0), 0.45);

    FragColor = vec4(color, 1.0);
}
