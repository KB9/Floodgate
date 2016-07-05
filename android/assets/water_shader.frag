#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
varying vec4 v_color;
varying vec2 v_texCoord;

uniform float resolution;
uniform float radius;
uniform vec2 dir;

const float LOWER_COLOR_CUTOFF = 0.05;
const float UPPER_COLOR_CUTOFF = 0.1;

vec4 gaussianBlur() {
    // RGBA sum
    vec4 sum = vec4(0.0);

    // Original texcoord for this fragment
    vec2 tc = v_texCoord;

    // The amount to blur, i.e. how far off center to sample from
    // 1.0 -> blur by one pixel
    // 2.0 -> blur by two pixels, etc
    float blur = radius / resolution;

    // The direction of the blur
    // (1.0, 0.0) -> x-axis blur
    // (0.0, 1.0) -> y-axis blur
    float hStep = dir.x;
    float vStep = dir.y;

    // Apply blurring, using a 9-tap filter with predefined Gaussian weights
    sum += texture2D(u_texture, vec2(tc.x - 4.0*blur*hStep, tc.y - 4.0*blur*vStep)) * 0.0162162162;
    sum += texture2D(u_texture, vec2(tc.x - 3.0*blur*hStep, tc.y - 3.0*blur*vStep)) * 0.0540540541;
    sum += texture2D(u_texture, vec2(tc.x - 2.0*blur*hStep, tc.y - 2.0*blur*vStep)) * 0.1216216216;
    sum += texture2D(u_texture, vec2(tc.x - 1.0*blur*hStep, tc.y - 1.0*blur*vStep)) * 0.1945945946;

    sum += texture2D(u_texture, vec2(tc.x, tc.y)) * 0.2270270270;

    sum += texture2D(u_texture, vec2(tc.x + 1.0*blur*hStep, tc.y + 1.0*blur*vStep)) * 0.1945945946;
    sum += texture2D(u_texture, vec2(tc.x + 2.0*blur*hStep, tc.y + 2.0*blur*vStep)) * 0.1216216216;
    sum += texture2D(u_texture, vec2(tc.x + 3.0*blur*hStep, tc.y + 3.0*blur*vStep)) * 0.0540540541;
    sum += texture2D(u_texture, vec2(tc.x + 4.0*blur*hStep, tc.y + 4.0*blur*vStep)) * 0.0162162162;

    return sum;
}

void main() {
    // Apply a Gaussian blur to the FBO texture
    vec4 result = gaussianBlur();

    // If the blurred pixel is not white enough, discard it
    if (result.r < LOWER_COLOR_CUTOFF) discard;

    result.a = 0.75;

    if (result.r < UPPER_COLOR_CUTOFF) {
        result.rgb = vec3(0.659, 0.871, 0.941);
    } else {
        result.rgb = vec3(0.251, 0.729, 0.890);
    }

    gl_FragColor = result;
}