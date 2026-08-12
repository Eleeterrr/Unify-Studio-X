#version 330 core

in vec4 vColor;
in vec2 vUV;

uniform sampler2D uTexture;
uniform int       uHasTexture;

out vec4 FragColor;

void main() {
    vec4 color;

    if (uHasTexture == 1) {
        color = texture(uTexture, vUV) * vColor;
    } else {
        float edgeFade = 1.0 - abs(vUV.x - 0.5) * 2.0;
        float alpha    = pow(clamp(edgeFade, 0.0, 1.0), 1.5);
        color = vec4(vColor.rgb, vColor.a * alpha);
    }

    if (color.a < 0.001) discard;
    FragColor = color;
}
