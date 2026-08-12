#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 3) in vec4 aIndices;
layout (location = 4) in vec4 aWeights;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uBones[150];

void main()
{
    vec4 totalLocalPos = vec4(0.0);
    vec3 totalNormal = vec3(0.0);
    float totalWeight = 0.0;

    for(int i = 0; i < 4; i++)
    {
        float weight = aWeights[i];
        if (weight <= 0.0) continue;
        int index = int(aIndices[i]);
        if (index < 0 || index >= 150) continue;

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

    // Slightly expand vertex along normal for outline
    vec3 displacedPos = totalLocalPos.xyz + totalNormal * 0.02;

    gl_Position = uProjection * uView * uModel * vec4(displacedPos, 1.0);
}
