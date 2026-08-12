#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uLightSpaceMatrix;


out vec3 vWorldPosition;
out vec3 vWorldNormal;
out vec2 vTexCoord;
out vec4 vFragPosLightSpace;

void main() {

    /*
    mat4 skinMatrix = mat4(1.0);
    if (uSkinned) {
        skinMatrix  = uBoneMatrices[aBoneIds.x] * aBoneWeights.x;
        skinMatrix += uBoneMatrices[aBoneIds.y] * aBoneWeights.y;
        skinMatrix += uBoneMatrices[aBoneIds.z] * aBoneWeights.z;
        skinMatrix += uBoneMatrices[aBoneIds.w] * aBoneWeights.w;
    }
    vec4 skinnedPos    = skinMatrix * vec4(aPosition, 1.0);
    vec4 skinnedNormal = skinMatrix * vec4(aNormal,   0.0);
    */

    vec4 worldPos  = uModel * vec4(aPosition, 1.0);

    mat3 normalMatrix = transpose(inverse(mat3(uModel)));

    vWorldPosition = worldPos.xyz;
    vWorldNormal   = normalize(normalMatrix * aNormal);
    vTexCoord      = aTexCoord;
    vFragPosLightSpace = uLightSpaceMatrix * worldPos;

    gl_Position = uProjection * uView * worldPos;

}
