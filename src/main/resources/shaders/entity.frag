#version 330 core

in vec3 vWorldPosition;
in vec3 vWorldNormal;
in vec2 vTexCoord;
in vec4 vFragPosLightSpace;

uniform vec3  uSunDirection;
uniform vec3  uSunColor;
uniform vec3  uAmbientColor;
uniform vec3  uCameraPos;

uniform bool  uFogEnabled;
uniform vec3  uFogColor;
uniform float uFogDensity;
uniform float uFogStart;
uniform float uFogEnd;

uniform bool uNightMode;

uniform mat4 uSpotLightSpaceMatrix;
uniform sampler2DShadow uSpotShadowMap;
uniform bool uHasSpotShadow;
uniform int uSpotShadowIndex;

#define MAX_SPOTLIGHTS 8

struct SpotLight
{
    vec3  position;
    vec3  direction;
    vec3  color;
    float innerCutoff;
    float outerCutoff;
    float range;
};

uniform SpotLight uSpot[MAX_SPOTLIGHTS];
uniform int       uSpotCount;

#define MAX_POINTLIGHTS 8

struct PointLight
{
    vec3  position;
    vec3  color;
    float range;
};

uniform PointLight uPoint[MAX_POINTLIGHTS];
uniform int        uPointCount;

uniform vec3  uBaseColor  = vec3(0.0, 0, 0);
uniform float uRoughness  = 0.8;
uniform float uMetallic   = 0.0;

uniform sampler2D uTexture;
uniform sampler2DShadow uShadowMap;
uniform bool      uHasTexture;

#define MAX_PARTICLE_LIGHTS 8

struct ParticleLight
{
    vec3  position;
    vec3  color;
    float range;
};

uniform ParticleLight uParticleLight[MAX_PARTICLE_LIGHTS];
uniform int           uParticleLightCount;

uniform mat4          uParticleLightShadowMatrix;
uniform sampler2DShadow uParticleLightShadowMap;
uniform bool          uHasParticleLightShadow;
uniform int           uParticleLightShadowIndex;

out vec4 fragColor;

float ShadowCalculation(vec4 fragPosLightSpace, vec3 normal, vec3 lightDir)
{
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    if(projCoords.z > 1.0 || projCoords.x < 0.0 || projCoords.x > 1.0 || projCoords.y < 0.0 || projCoords.y > 1.0)
        return 0.0;

    float currentDepth = projCoords.z;
    float bias = max(0.005 * (1.0 - dot(normal, lightDir)), 0.001);
    
    vec2 poissonDisk[16] = vec2[](
       vec2(-0.94201624, -0.39906216), vec2(0.94558609, -0.76890725), vec2(-0.094184101, -0.92938870), vec2(0.34495938, 0.29387760),
       vec2(-0.91588581, 0.45771432), vec2(-0.81544232, -0.87912464), vec2(-0.38277543, 0.27676845), vec2(0.97484398, 0.75648379),
       vec2(0.44323325, -0.97511554), vec2(0.53742981, -0.47373420), vec2(-0.26496911, -0.41893023), vec2(0.79197514, 0.19090188),
       vec2(-0.24188840, 0.99706507), vec2(-0.81409955, 0.91437590), vec2(0.19984126, 0.78641367), vec2(0.14383161, -0.14100465)
    );

    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(uShadowMap, 0);
    float filterRadius = 1.5;

    for (int i = 0; i < 16; i++)
    {
        float shadowFactor = texture(uShadowMap, vec3(projCoords.xy + poissonDisk[i] * texelSize * filterRadius, currentDepth - bias)); 
        shadow += (1.0 - shadowFactor);        
    }
    
    return shadow / 16.0;
}

vec3 blinnPhong(vec3 N, vec3 L, vec3 V, vec3 albedo, float shadow)
{
    float NdotL  = max(dot(N, L), 0.0);
    vec3  H = normalize(L + V);
    float spec   = pow(max(dot(N, H), 0.0), mix(4.0, 128.0, 1.0 - uRoughness));

    vec3 diffuse  = uSunColor * albedo * NdotL * (1.0 - shadow);
    vec3 specular = uSunColor * spec   * (1.0 - uRoughness) * mix(vec3(1.0), albedo, uMetallic) * (1.0 - shadow);
    vec3 ambient  = uAmbientColor * albedo;

    return ambient + diffuse + specular;
}

vec3 calcSpotlightContrib(vec3 worldPos, vec3 N, vec3 V, vec3 albedo)
{
    vec3 total = vec3(0.0);
    for (int i = 0; i < uSpotCount; i++)
    {
        vec3  toLight = uSpot[i].position - worldPos;
        float dist    = length(toLight);

        // Early-out: beyond light range
        if (dist > uSpot[i].range) continue;

        vec3  L     = toLight / dist;
        vec3  H     = normalize(L + V);
        float NdotL = max(dot(N, L), 0.0);

        float normalizedDist = clamp(dist / uSpot[i].range, 0.0, 1.0);
        float distanceWindow = 1.0 - pow(normalizedDist, 4.0);
        float attenuation = distanceWindow * distanceWindow; // Smooth falloff from 1 to 0

       
        float theta   = dot(L, -uSpot[i].direction);
        float epsilon = uSpot[i].innerCutoff - uSpot[i].outerCutoff;
        float cone    = smoothstep(0.0, 1.0, clamp((theta - uSpot[i].outerCutoff) / epsilon, 0.0, 1.0));

  
        float specExponent = mix(4.0, 128.0, 1.0 - uRoughness);
        float spec = pow(max(dot(N, H), 0.0), specExponent);
        vec3 specularColor = mix(vec3(1.0), albedo, uMetallic);
        vec3 specular = specularColor * spec * (1.0 - uRoughness);

       
        float spotShadow = 0.0;
    
        if (cone > 0.0 && uHasSpotShadow && i == uSpotShadowIndex) {
            vec4 fragPosSpotSpace = uSpotLightSpaceMatrix * vec4(worldPos, 1.0);
          
            if (fragPosSpotSpace.w > 0.0) {
                vec3 projCoords = fragPosSpotSpace.xyz / fragPosSpotSpace.w;
                projCoords = projCoords * 0.5 + 0.5;
                
                
                if(projCoords.z >= 0.0 && projCoords.z <= 1.0 &&
                   projCoords.x >= 0.0 && projCoords.x <= 1.0 &&
                   projCoords.y >= 0.0 && projCoords.y <= 1.0) {
                    float bias = max(0.005 * (1.0 - NdotL), 0.0005);
                    float spotShadowFactor = 0.0;
                    vec2 texelSize = 1.0 / textureSize(uSpotShadowMap, 0);
                    for(int x = -1; x <= 1; x+=2) {
                        for(int y = -1; y <= 1; y+=2) {
                            spotShadowFactor += texture(uSpotShadowMap, vec3(projCoords.xy + vec2(x, y) * texelSize, projCoords.z - bias));
                        }
                    }
                    spotShadow = 1.0 - (spotShadowFactor / 4.0);
                }
            }
        }

        total += uSpot[i].color * (albedo * NdotL + specular) * cone * attenuation * (1.0 - spotShadow);
    }
    return total;
}

vec3 calcPointLightContrib(vec3 worldPos, vec3 N, vec3 V, vec3 albedo)
{
    vec3 total = vec3(0.0);
    for (int i = 0; i < uPointCount; i++)
    {
        vec3  toLight = uPoint[i].position - worldPos;
        float dist    = length(toLight);

        
        if (dist > uPoint[i].range) continue;

        vec3  L     = toLight / dist;
        vec3  H     = normalize(L + V);
        float NdotL = max(dot(N, L), 0.0);

       
        float normalizedDist = clamp(dist / uPoint[i].range, 0.0, 1.0);
        float distanceWindow = 1.0 - pow(normalizedDist, 4.0);
        float attenuation = distanceWindow * distanceWindow;

      
        float specExponent = mix(4.0, 128.0, 1.0 - uRoughness);
        float spec = pow(max(dot(N, H), 0.0), specExponent);
        vec3 specularColor = mix(vec3(1.0), albedo, uMetallic);
        vec3 specular = specularColor * spec * (1.0 - uRoughness);

        total += uPoint[i].color * (albedo * NdotL + specular) * attenuation;
    }
    return total;
}

vec3 calcParticleLightContrib(vec3 worldPos, vec3 N, vec3 V, vec3 albedo)
{
    vec3 total = vec3(0.0);

    for (int i = 0; i < uParticleLightCount; i++)
    {
        vec3  toLight = uParticleLight[i].position - worldPos;
        float dist    = length(toLight);

        if (dist > uParticleLight[i].range) continue;

        vec3  L     = toLight / dist;
        float NdotL = max(dot(N, L), 0.0);

     
        float nd   = clamp(dist / uParticleLight[i].range, 0.0, 1.0);
        float atten = (1.0 - nd * nd) * (1.0 - nd * nd);

        vec3  H    = normalize(L + V);
        float spec = pow(max(dot(N, H), 0.0), mix(4.0, 64.0, 1.0 - uRoughness));
        vec3  specular = mix(vec3(1.0), albedo, uMetallic) * spec * (1.0 - uRoughness) * 0.4;

        float particleShadow = 0.0;
        if (uHasParticleLightShadow && i == uParticleLightShadowIndex)
        {
            vec4 fragPosLightSpace = uParticleLightShadowMatrix * vec4(worldPos, 1.0);
            if (fragPosLightSpace.w > 0.0)
            {
                vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
                projCoords = projCoords * 0.5 + 0.5;

                if (projCoords.z >= 0.0 && projCoords.z <= 1.0 &&
                    projCoords.x >= 0.0 && projCoords.x <= 1.0 &&
                    projCoords.y >= 0.0 && projCoords.y <= 1.0)
                {
                    float bias = max(0.005 * (1.0 - NdotL), 0.0005);
                    float shadowFactor = 0.0;
                    vec2 texelSize = 1.0 / textureSize(uParticleLightShadowMap, 0);

                    for (int x = -1; x <= 1; x += 2)
                    {
                        for (int y = -1; y <= 1; y += 2)
                        {
                            shadowFactor += texture(uParticleLightShadowMap,
                                vec3(projCoords.xy + vec2(x, y) * texelSize, projCoords.z - bias));
                        }
                    }

                    particleShadow = 1.0 - shadowFactor / 4.0;
                }
            }
        }

        total += uParticleLight[i].color * (albedo * NdotL + specular) * atten * (1.0 - particleShadow);
    }

    return total;
}

vec3 applyFog(vec3 colour, float dist)
{
    float linFactor = (uFogEnd - dist) / (uFogEnd - uFogStart);
    
    float expFactor = exp(-pow(uFogDensity * dist, 2.0));
    
    float factor = clamp(min(linFactor, expFactor), 0.0, 1.0);
    
    return mix(uFogColor, colour, factor);
}


void main()
{
    vec3 N = normalize(vWorldNormal);
    vec3 L = normalize(-uSunDirection);
    vec3 V = normalize(uCameraPos - vWorldPosition);

    vec3 albedo = uBaseColor;
    if (uHasTexture)
    {
        albedo *= texture(uTexture, vTexCoord).rgb;
    }

    float shadow = 0.0;
    if (!uNightMode) {
        shadow = ShadowCalculation(vFragPosLightSpace, N, L);
    }

    vec3 colour = blinnPhong(N, L, V, albedo, shadow);
    colour += calcSpotlightContrib(vWorldPosition, N, V, albedo);
    colour += calcPointLightContrib(vWorldPosition, N, V, albedo);
    colour += calcParticleLightContrib(vWorldPosition, N, V, albedo);

    if (uFogEnabled)
    {
        float dist = length(uCameraPos - vWorldPosition);
        colour = applyFog(colour, dist);
    }

    colour = pow(colour, vec3(1.0 / 2.2));

    fragColor = vec4(colour, 1.0);
}
