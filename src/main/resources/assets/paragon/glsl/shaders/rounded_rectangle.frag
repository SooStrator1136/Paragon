// made by shoroa

#version 120

uniform vec2 size;
uniform vec4 colour;
uniform float radius;

// yeah ok what the fuck
uniform float alpha;

float roundedBoxSDF(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

void main() {
    vec2 h = size * 0.5;
    float smoothed = (1.0 - smoothstep(0.0, 1.0, roundedBoxSDF(h - (gl_TexCoord[0].st * size), h - radius - 1.0, radius))) * alpha;

    gl_FragColor = vec4(colour.rgb, smoothed);
}