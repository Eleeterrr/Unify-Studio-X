#version 330 core

in vec3 vColor;
in vec3 vWorldPos;

out vec4 fragColor;

void main()
{
    vec3 dx = dFdx(vWorldPos);
    vec3 dy = dFdy(vWorldPos);
    vec3 crossProd = cross(dx, dy);
    
    float len = length(crossProd);
    vec3 normal = (len > 0.00001) ? (crossProd / len) : vec3(0.0, 1.0, 0.0);
    
    vec3 lightDir = normalize(vec3(0.5, 1.0, 0.5));
    float diff = max(dot(normal, lightDir), 0.0);
    
    float ambient = 0.4;
    float light = ambient + diff * 0.6;
    
    fragColor = vec4(vColor * light, 1.0);
}
