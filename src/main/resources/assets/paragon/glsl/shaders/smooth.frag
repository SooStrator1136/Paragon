/*
    Written by linustouchtips and Wolfsurge
*/
#version 120

uniform sampler2D texture;
uniform vec2 texelSize;
uniform vec2 playerRotation;
uniform float radius;
uniform int colour;
uniform int outline;

void main(void) {
    vec4 col = texture2D(texture, gl_TexCoord[0].xy);
    vec3 d = normalize(vec3((gl_FragCoord.xy - 1 * 0.5) / 1, 0.5));

    vec3 useColour;

    if (colour == 1) {
        useColour = normalize(vec3((gl_FragCoord.xy - 1 * 0.5) / 1, 0.5));
        vec3 rotationHSV = vec3((playerRotation.x + 180) / 360, 1, 1);
    } else if (colour > 1 && colour < 4) {
        float pitch = ((playerRotation.y + 90) / 180F) * 360F;
        float rotation = colour == 2 ? playerRotation.x : pitch;

        vec3 rotationHSV = vec3((rotation + 180) / 360, 1, 1);

        vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
        vec3 p = abs(fract(rotationHSV.xxx + K.xyz) * 6.0 - K.www);
        useColour = rotationHSV.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), rotationHSV.y);
    }

    if (col.a > 0) {
        gl_FragColor = vec4(useColour.x, useColour.y, useColour.z, 0.5F);
    } else {
        if (outline == 1) {
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

            gl_FragColor = vec4(useColour.x, useColour.y, useColour.z, (radius - (closest - radius)) / radius);
        }
    }
}