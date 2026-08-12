#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aWeights;
layout (location = 4) in vec4 aIndices;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uBones[100];

void main()
{
    vec4 totalLocalPos = vec4(0.0);
    float totalWeight = 0.0;

    for(int i = 0; i < 4; i++)
    {
        float weight = aWeights[i];
        if(weight <= 0.0)
        {
            continue;
        }

        int index = int(aIndices[i]);
        if (index < 0 || index >= 100)
        {
            continue;
        }

        mat4 boneTransform = uBones[index];
        vec4 posePos = boneTransform * vec4(aPos, 1.0);
        totalLocalPos += posePos * weight;

        totalWeight += weight;
    }

    if (totalWeight < 0.001)
    {
        totalLocalPos = vec4(aPos, 1.0);
    } 
    else
    {
        totalLocalPos /= totalWeight;
    }

    vec4 worldPos = uModel * totalLocalPos;
    gl_Position = uProjection * uView * worldPos;
}
