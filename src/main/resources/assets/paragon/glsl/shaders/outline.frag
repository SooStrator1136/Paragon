#version 120

uniform sampler2D texture;
uniform vec2 texelSize;

// Colour and width
uniform vec4 colour;
uniform float radius;

uniform int fill;

void main() {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    if (centerCol.a > 0) {
        if (fill == 1) {
            gl_FragColor = vec4(colour.x, colour.y, colour.z, 0.5F);
        } else {
            gl_FragColor = vec4(0, 0, 0, 0);
        }
    } else {
        float closest = radius * 2.0F + 2.0F;

        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius; y <= radius; y++) {

                vec4 currentcolour = texture2D(texture, gl_TexCoord[0].xy + vec2(texelSize.x * x, texelSize.y * y));

                if (currentcolour.a > 0) {
                    float currentDist = sqrt(x * x + y * y);

                    if (currentDist < closest) {
                        closest = currentDist;
                    }
                }
            }
        }

        gl_FragColor = vec4(colour.x, colour.y, colour.z, (radius - (closest - radius)) / radius);
    }
}
