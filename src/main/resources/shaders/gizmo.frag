#version 330 core

in  vec3 vColor;
out vec4 fragColor;

uniform int uHighlightAxis;
uniform int uActiveAxis;


int dominantAxis(vec3 c)
{
    /* X handle is primarily Red */
    if (c.r > 0.8 && c.g < 0.6 && c.b < 0.6) return 1;
    /* Y handle is primarily Green */
    if (c.g > 0.8 && c.r < 0.6 && c.b < 0.6) return 2;
    /* Z handle is primarily Blue */
    if (c.b > 0.8 && c.r < 0.6 && c.g < 0.7) return 3;
    /* Center handle is Yellow (1,1,0.1) or White (1,1,1) */
    if (c.r > 0.9 && c.g > 0.9) return 4;
    
    return 0;
}

void main()
{
    vec3 color = vColor;
    int  axis  = dominantAxis(vColor);

    if (axis != 0)
    {
        if (axis == 4)
        {
            if (uActiveAxis == 4 || uHighlightAxis == 4)
            {
                color = vec4(1.0, 1.0, 1.0, 1.0).rgb;
            }
        }
        else
        {
            if (axis == uActiveAxis)
            {
                color = mix(color, vec3(1.0, 1.0, 0.15), 0.90);
            }
            else if (axis == uHighlightAxis)
            {
                color = mix(color, vec3(1.0, 0.82, 0.10), 0.65);
            }
        }
    }

    fragColor = vec4(color, 1.0);
}
