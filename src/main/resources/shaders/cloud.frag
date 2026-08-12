#version 330 core

in vec3 vLocalPos;

uniform float uTime;
uniform float uCoverage;
uniform float uSpeed;
uniform float uDensity;
uniform float uAltitude;
uniform vec3  uWindDir;
uniform vec3  uSunDir;
uniform vec3  uSunColor;
uniform bool  uNightMode;

out vec4 fragColor;

float hash(vec2 p)
{
    vec2 q = fract(p * vec2(443.897, 441.423));
    q += dot(q, q + 19.19);
    return fract(q.x * q.y);
}

float smoothNoise(vec2 p)
{
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x), mix(hash(i + vec2(0.0,1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p)
{
    float val = 0.0;
    float amp = 0.5;
    mat2 rot = mat2(0.80, 0.60, -0.60, 0.80);
    for (int i = 0; i < 7; i++)
    {
        val += amp * smoothNoise(p);
        p = rot * p * 2.03 + vec2(1.72, 9.19);
        amp *= 0.48;
    }
    return val;
}

void main()
{
    vec3 viewDir = normalize(vLocalPos);
    float altitude = viewDir.y;

    if (altitude < uAltitude - 0.05)
    {
        discard;
    }

    float safeY   = max(altitude, 0.04);
    vec2  cloudUV = clamp(viewDir.xz / safeY, vec2(-18.0), vec2(18.0));

    vec2 scroll = uWindDir.xz * uTime * uSpeed;
    vec2 uv = cloudUV * 2.2 + scroll;

    vec2 q = vec2(fbm(uv + vec2(0.00, 0.00)), fbm(uv + vec2(5.20, 1.30)));

    vec2 r = vec2(fbm(uv + 1.1 * q + vec2(1.70, 9.20)), fbm(uv + 1.1 * q + vec2(8.30, 2.80)));

    float shape  = fbm(uv + 0.9 * r);
    float detail  = fbm(uv * 2.1 + scroll * 0.5 + vec2(3.3, 7.1)) * 0.50+ fbm(uv * 4.3 + scroll * 0.3 + vec2(1.1, 4.7)) * 0.25;
    shape = shape * 0.75 + detail * 0.25;

    float cloud = smoothstep(1.0 - uCoverage, 1.0, shape);

    if (cloud < 0.001)
    {
        discard;
    }

    vec3  towardSun = normalize(-uSunDir);
    float sunHeight = clamp(towardSun.y, 0.0, 1.0);

    vec3 cloudColor;

    if (uNightMode)
    {
        vec3 nightTop = vec3(0.18, 0.20, 0.30) + uSunColor * 0.25;
        vec3 nightBottom = vec3(0.04, 0.05, 0.09);
        cloudColor = mix(nightBottom, nightTop, cloud * 0.7 + 0.1 * shape);
    } else
    {
        vec3 sunsetTint = mix(vec3(1.0, 0.50, 0.20), vec3(1.0, 1.0, 1.0), sunHeight);
        vec3 topColor = mix(vec3(0.82, 0.85, 0.90), vec3(1.0, 0.98, 0.96), sunHeight)+ uSunColor * 0.15 * sunHeight;
        vec3 bottomColor = mix(vec3(0.32, 0.35, 0.44), vec3(0.54, 0.57, 0.64), sunHeight);

        float lightGrad = cloud * (0.45 + 0.55 * shape);
        cloudColor = mix(bottomColor, topColor, lightGrad);
        cloudColor = mix(cloudColor, cloudColor * sunsetTint, 1.0 - sunHeight);

        float silverMask = smoothstep(0.0, 0.35, cloud) * (1.0 - smoothstep(0.35, 0.80, cloud));
        float sunFacing = max(0.0, dot(normalize(viewDir.xz), normalize(towardSun.xz)));
        cloudColor += vec3(0.90, 0.94, 1.00) * silverMask * sunFacing * sunHeight * 0.45;
    }

    float alpha = cloud * uDensity;
    alpha *= smoothstep(uAltitude - 0.02, uAltitude + 0.12, altitude);
    alpha *= smoothstep(0.0, 0.28, altitude);
    alpha  = clamp(alpha, 0.0, 0.92);

    if (alpha < 0.003)
    {
        discard;
    }

    fragColor = vec4(cloudColor, alpha);
}
