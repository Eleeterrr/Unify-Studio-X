#version 330 core

in vec3 vLocalPos;

uniform vec3  uTopColor;
uniform vec3  uBottomColor;
uniform vec3  uHorizonColor;

uniform float uHaze;
uniform float uSunSize;
uniform vec3  uSunDir;
uniform bool  uNightMode;
uniform float uTime;

out vec4 fragColor;

float hash(vec3 p)
{
    p = fract(p * vec3(443.897, 441.423, 437.195));
    p += dot(p, p.yzx + 19.19);
    return fract((p.x + p.y) * p.z);
}

vec3 hash33(vec3 p) {
    p = vec3(dot(p, vec3(127.1, 311.7, 74.7)),
             dot(p, vec3(269.5, 183.3, 246.1)),
             dot(p, vec3(113.5, 271.9, 124.6)));
    return fract(sin(p) * 43758.5453123);
}

float smoothNoise(vec3 p)
{
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(mix(hash(i),               hash(i + vec3(1,0,0)), u.x),
            mix(hash(i + vec3(0,1,0)), hash(i + vec3(1,1,0)), u.x), u.y),
        mix(mix(hash(i + vec3(0,0,1)), hash(i + vec3(1,0,1)), u.x),
            mix(hash(i + vec3(0,1,1)), hash(i + vec3(1,1,1)), u.x), u.y),
        u.z);
}

float fbm(vec3 p)
{
    float v = 0.0;
    float a = 0.5;
    vec3 shift = vec3(1.72, 9.2, -4.71);
    for (int i = 0; i < 5; i++)
    {
        v += a * smoothNoise(p);
        p = p * 2.1 + shift;
        a *= 0.5;
    }
    return v;
}

vec2 voronoiStar(vec3 p, float scale)
{
    p *= scale;
    vec3 i = floor(p);
    vec3 f = fract(p);
    float minDist = 1.0;
    float cellHash = 0.0;
    for (int x = -1; x <= 1; x++)
    {
        for (int y = -1; y <= 1; y++)
        {
            for (int z = -1; z <= 1; z++)
            {
                vec3 nb = vec3(float(x), float(y), float(z));
                vec3 r = hash33(i + nb);
                vec3 diff = nb + r - f;
                float d = length(diff);
                if (d < minDist)
                {
                    minDist = d;
                    cellHash = r.x;
                }
            }
        }
    }
    return vec2(minDist, cellHash);
}

vec3 starColor(float t)
{
    vec3 warm  = vec3(1.0,  0.55, 0.25);
    vec3 white = vec3(1.0,  0.97, 0.90);
    vec3 cool  = vec3(0.70, 0.82, 1.0);
    if (t < 0.5) return mix(warm, white, t * 2.0);
    return mix(white, cool, (t - 0.5) * 2.0);
}

vec3 starsLayer(vec3 dir, float scale, float threshold, float bloomK, float sharpK, float brightness, float fadeH)
{
    vec2 v = voronoiStar(dir, scale);
    float bri = hash(floor(dir * scale) + vec3(7.3, 2.1, 5.8));
    if (v.x > 0.12 || bri <= threshold) return vec3(0.0);
    float phase = v.y * 6.28318 + uTime * (0.4 + v.y * 2.5);
    float twinkle = 0.80 + 0.20 * sin(phase);
    float norm = (bri - threshold) / (1.0 - threshold);
    norm = pow(norm, 0.5) * twinkle;
    float bloom = exp(-v.x * v.x * bloomK);
    float sharp = exp(-v.x * v.x * sharpK);
    float val = (sharp + bloom * 0.4) * norm * brightness;
    return starColor(v.y) * val * max(0.0, 1.0 - fadeH * 4.0);
}

void main()
{
    vec3 viewDir = normalize(vLocalPos);

    float y = viewDir.y;
    vec3 skyBase;

    if (y > 0.0)
    {
        skyBase = mix(uHorizonColor, uTopColor, pow(y, 1.0 / (1.0 + uHaze * 2.0)));
    } else
    {
        skyBase = mix(uHorizonColor, uBottomColor, pow(-y, 1.0 / (1.0 + uHaze * 0.5)));
    }

    float sunTheta = max(dot(viewDir, normalize(uSunDir)), 0.0);

    float glow = pow(sunTheta, 256.0 / uSunSize);

    float disk = pow(sunTheta, 2048.0 / uSunSize);

    vec3 sunColor = vec3(1.0, 0.9, 0.7) * (disk * 2.0 + glow * 0.5);

    float horizonFactor = pow(1.0 - abs(y), 1.0 + uHaze * 5.0);
    vec3 finalSky = mix(skyBase, uHorizonColor, horizonFactor * uHaze);

    if (uNightMode)
    {
        float moonSize = uSunSize * 0.4;
        float moonAngle = acos(clamp(dot(viewDir, normalize(uSunDir)), -1.0, 1.0));
        float moonDisk  = pow(sunTheta, 2048.0 / moonSize);
        float corona    = exp(-moonAngle * 14.0) * 0.45;
        float halo      = exp(-moonAngle *  3.5) * 0.10;
        float farGlow   = exp(-moonAngle *  1.2) * 0.03;

        vec3 moonCol   = vec3(0.96, 0.93, 0.88);
        vec3 coronaCol = vec3(0.78, 0.86, 1.0);
        vec3 haloCol   = vec3(0.45, 0.58, 0.90);

        sunColor = moonCol * moonDisk * 2.6+ coronaCol * corona * (1.0 - moonDisk)+ haloCol   * halo+ haloCol   * farGlow;

        if (y > -0.04)
        {
            float hFade = clamp(horizonFactor, 0.0, 1.0);

            sunColor += starsLayer(viewDir,  80.0, 0.920, 500.0,  5000.0, 3.0, hFade);
            sunColor += starsLayer(viewDir, 200.0, 0.888, 1000.0, 9000.0, 1.8, hFade);
            sunColor += starsLayer(viewDir, 420.0, 0.860, 2500.0,20000.0, 1.0, hFade);
            sunColor += starsLayer(viewDir, 750.0, 0.840, 6000.0,40000.0, 0.6, hFade);

            vec3 galAxis = normalize(vec3(0.495, 0.819, 0.287));
            float galLat = abs(dot(viewDir, galAxis));
            float galWidth = smoothstep(0.42, 0.0, galLat);

            if (galWidth > 0.001)
            {
                float mw  = fbm(viewDir * 3.8 + vec3(1.3, 2.7, 0.9));
                float mw2 = fbm(viewDir * 7.1 - vec3(3.1, 0.5, 2.2));
                float band = mw * mw * (0.6 + 0.4 * mw2);
                float mwI  = galWidth * band * 0.22;
                vec3 mwCol = mix(vec3(0.55, 0.62, 0.95), vec3(0.80, 0.72, 0.98), mw2);
                sunColor += mwCol * mwI * max(0.0, 1.0 - hFade * 2.5);
            }
        }

        float horizonGlow = exp(-abs(y) * 7.0) * 0.055;
        finalSky += vec3(0.04, 0.07, 0.22) * horizonGlow;
    }

    vec3 finalColor = finalSky + sunColor;

    fragColor = vec4(finalColor, 1.0);
}
