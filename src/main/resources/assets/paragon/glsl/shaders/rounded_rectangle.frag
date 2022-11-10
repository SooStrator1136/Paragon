#version 120

uniform vec2 size;
uniform vec4 colour;
uniform float radius;

void main() {
    gl_FragColor = vec4(colour.rgb, (1.0f - smoothstep(0.0f, 1.0f, length(max(abs((gl_TexCoord[0].xy * size).xy - (size / 2.0f)) - (size / 2.0f) + radius, 0.0)) - radius)) * colour.a);
}