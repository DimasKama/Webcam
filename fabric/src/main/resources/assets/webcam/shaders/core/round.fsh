#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0 || length((texCoord0 - 0.5)) > 0.5) {
        discard;
    }
    fragColor = color * ColorModulator;
}
