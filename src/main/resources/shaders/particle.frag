#version 330 core

in vec4 vColor;
in vec2 vTexCoord;

uniform sampler2D uTexture;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(uTexture, vTexCoord);
    
    if (texColor.a * vColor.a < 0.001)
    {
        discard;
    }
    
    fragColor = texColor * vColor;
}