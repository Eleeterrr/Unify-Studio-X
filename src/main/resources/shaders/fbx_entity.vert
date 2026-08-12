#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in vec4 aIndices;
layout (location = 4) in vec4 aWeights;

out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vWorldPos;
out vec4 vFragPosLightSpace;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uLightSpaceMatrix;
uniform mat4 uBones[150];

void main()
{
    vec4 totalLocalPos = vec4(0.0);
    vec3 totalNormal = vec3(0.0);
    float totalWeight = 0.0;

    for(int i = 0; i < 4; i++)
    {
        float weight = aWeights[i];
        if (weight <= 0.0)
        {
            continue;
        }

        int index = int(aIndices[i]);
        if (index < 0 || index >= 150)
        {
            continue;
        }

        mat4 boneTransform = uBones[index];
        vec4 posePos = boneTransform * vec4(aPos, 1.0);
        totalLocalPos += posePos * weight;

        vec3 worldNormal = mat3(boneTransform) * aNormal;
        totalNormal += worldNormal * weight;
        totalWeight += weight;
    }

    if (totalWeight < 0.001)
    {
        totalLocalPos = vec4(aPos, 1.0);
        totalNormal = aNormal;
    } else
    {
        totalLocalPos /= totalWeight;
        totalNormal = normalize(totalNormal);
    }

    vTexCoord = aTexCoord;
    vNormal = mat3(uModel) * normalize(totalNormal);
    
    vec4 worldPos = uModel * totalLocalPos;
    vWorldPos = worldPos.xyz;
    vFragPosLightSpace = uLightSpaceMatrix * worldPos;

    gl_Position = uProjection * uView * worldPos;
}
