#version 330 core

in vec4 vColor;
in vec2 vUV;

uniform sampler2D uTexture;
uniform int       uHasTexture;
uniform int       uSoftParticle;
uniform sampler2D uDepthTex;
uniform vec2      uScreenSize;

out vec4 FragColor;

void main() {
    vec4 color;

    if (uHasTexture == 1) {
        color = texture(uTexture, vUV) * vColor;
    } else {
        float dist  = length(vUV - 0.5) * 2.0;
        float alpha = pow(clamp(1.0 - dist, 0.0, 1.0), 2.0);
        color = vec4(vColor.rgb, vColor.a * alpha);
    }

    if (uSoftParticle == 1) {
        vec2  screenUV   = gl_FragCoord.xy / uScreenSize;
        float sceneDepth = texture(uDepthTex, screenUV).r;
        float fade       = clamp((sceneDepth - gl_FragCoord.z) * 30.0, 0.0, 1.0);
        color.a         *= fade;
    }

    if (color.a < 0.001) discard;
    FragColor = color;
}
