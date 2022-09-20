/*
 * Original shader from: https://www.shadertoy.com/view/wtd3zM
 */

#ifdef GL_ES
precision mediump float;
#endif

// glslsandbox uniforms
uniform vec3 colour;
uniform float time;
uniform vec2 resolution;

// shadertoy emulation
#define iTime time
#define iResolution resolution
const vec4 iMouse = vec4(0.0);

// --------[ Original ShaderToy begins here ]---------- //
// Based on theGiallo's https://www.shadertoy.com/view/MttSz2
// MIT License. Use freely; but attribution is expected.
#define TAU 6.28318
const float period = 0.0;
const float speed  = 1.0;
const float rotation_speed = 0.1;
const float t2 = 100.0; // Length in seconds of the effect

// This effect fades in and out of white every t2 seconds
// Remove the next def to get an infinite tunnel instead.
//#define WHITEOUT 1

// From https://www.shadertoy.com/view/4sc3z2
// and https://www.shadertoy.com/view/XsX3zB
#define MOD3 vec3(.1031,.11369,.13787)
vec3 hash33(vec3 p3)
{
    p3 = fract(p3 * MOD3);
    p3 += dot(p3, p3.yxz+19.19);
    return -1.0 + 2.0 * fract(vec3((p3.x + p3.y)*p3.z, (p3.x+p3.z)*p3.y, (p3.y+p3.z)*p3.x));
}

float simplexNoise(vec3 p)
{
    const float K1 = 0.333333333;
    const float K2 = 0.166666667;

    vec3 i = floor(p + (p.x + p.y + p.z) * K1);
    vec3 d0 = p - (i - (i.x + i.y + i.z) * K2);

    vec3 e = step(vec3(0.0), d0 - d0.yzx);
    vec3 i1 = e * (1.0 - e.zxy);
    vec3 i2 = 1.0 - e.zxy * (1.0 - e);

    vec3 d1 = d0 - (i1 - 1.0 * K2);
    vec3 d2 = d0 - (i2 - 2.0 * K2);
    vec3 d3 = d0 - (1.0 - 3.0 * K2);

    vec4 h = max(0.6 - vec4(dot(d0, d0), dot(d1, d1), dot(d2, d2), dot(d3, d3)), 0.0);
    vec4 n = h * h * h * h * vec4(dot(d0, hash33(i)), dot(d1, hash33(i + i1)), dot(d2, hash33(i + i2)), dot(d3, hash33(i + 1.0)));

    return dot(vec4(31.316), n);
}

float fBm3(in vec3 p)
{
    //p += vec2(sin(iTime * 0.1), cos(iTime * 0.15))*(.1) + iMouse.xy*.1/iResolution.xy;
    float f = 0.0;
    // Change starting scale to any integer value...
    float scale = 13.0;
    p = mod(p, scale);
    float amp   = 0.75;

    for (int i = 0; i < 5; i++)
    {
        f += simplexNoise(p * scale) * amp;
        amp *= 0.5;
        // Scale must be multiplied by an integer value...
        scale *= 2.0;
    }
    // Clamp it just in case....
    return min(f, 1.0);
}


void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    float t = mod(iTime, t2);
    t = t / t2; // Normalized time

    vec4 col = vec4(0.0);
    vec2 q = fragCoord.xy / iResolution.xy;
    vec2 p = ( 2.0 * fragCoord.xy - iResolution.xy ) / min( iResolution.y, iResolution.x );
    vec2 mo = (2.0 * iMouse.xy - iResolution.xy) / min(iResolution.x, iResolution.y);
    p += vec2(0.0, -0.1);

    //float ay = TAU * mod(iTime, 8.0) / 8.0;
    //ay = 45.0 * 0.01745;
    float ay = 0.0, ax = 0.0, az = 0.0;
    if (iMouse.z > 0.0) {
        ay = 3.0 * mo.x;
        ax = 3.0 * mo.y;
    }
    mat3 mY = mat3(
    cos(ay), 0.0,  sin(ay),
    0.0,     1.0,      0.0,
    -sin(ay), 0.0,  cos(ay)
    );

    mat3 mX = mat3(
    1.0,      0.0,     0.0,
    0.0,  cos(ax), sin(ax),
    0.0, -sin(ax), cos(ax)
    );
    mat3 m = mX * mY;

    vec3 v = vec3(p, 1.0);
    v = m * v;
    float v_xy = length(v.xy);
    float z = v.z / v_xy;

    // The focal_depth controls how "deep" the tunnel looks. Lower values
    // provide more depth.
    float focal_depth = 0.25;
    #ifdef WHITEOUT
    focal_depth = mix(5.51, 0, smoothstep(1.65, 5.0, t));
    #endif

    vec2 polar;
    //float p_len = length(p);
    float p_len = length(v.xy);
    //polar.y = focal_depth / p_len + iTime * speed;
    polar.y = z * focal_depth + iTime * speed;
    float a = atan(v.y, v.x);
    a -= iTime * rotation_speed;
    float x = fract(a / TAU);
    // Remove the seam by reflecting the u coordinate around 0.5:
    if (x >= 0.5) x = 1.0 - x;
    polar.x = x * 1.0; // Original period: 4

    // Colorize blue
    //col = texture(iChannel1, cp);
    float val = 0.45 + 0.55 * fBm3(
    vec3(vec2(1.0, 0.2) * polar, 0.05 * iTime));
    val = clamp(val, 0.0, 1.0);

    // COLOUR TWEAKING!!

    col.rgb = colour * vec3(val);

    // Add white spots
    vec3 white = 0.35 * vec3(smoothstep(0.55, 1.0, val));
    col.rgb += white;
    col.rgb = clamp(col.rgb, 0.0, 1.0);

    float w_total = 0.0, w_out = 0.0;
    #ifdef WHITEOUT
    // Fade in and out from white every t2 seconds
    float w_in = 0.0;
    w_in = abs(1.0 - 1.0 * smoothstep(0.0, 0.25, t));
    w_out = abs(1.0 * smoothstep(0.8, 1.0, t));
    w_total = max(w_in, w_out);
    #endif


    // Add the white disk at the center
    float disk_size = max(0.025, 1.5 * w_out);
    //disk_size = 0.001;
    float disk_col = exp(-(p_len - disk_size) * 4.0);
    //col.rgb += mix(col.xyz, vec3(1,1,1), disk_col);
    col.rgb += clamp(vec3(disk_col), 0.0, 1.0);


    #ifdef WHITEOUT
    col.rgb = mix(col.rgb, vec3(1.0), w_total);
    #endif

    fragColor = vec4(col.rgb,1);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
    gl_FragColor.a = 1.0;
}