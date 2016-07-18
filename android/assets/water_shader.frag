#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
varying vec4 v_color;
varying vec2 v_texCoord;

uniform vec2 u_size;

const float LOWER_COLOR_CUTOFF = 0.05;
const float ALPHA_CUTOFF = 0.1;
const float UPPER_COLOR_CUTOFF = 0.5;

// Credit: http://rastergrid.com/blog/2010/09/efficient-gaussian-blur-with-linear-sampling/
vec4 gaussianBlur() {

    // The up/down offsets where the original image will also be drawn
    float offset[3];
    offset[0] = 0.0;
    offset[1] = 1.3846153846;
    offset[2] = 3.2307692308;

    float weight[3];
    weight[0] = 0.2270270270;
    weight[1] = 0.3162162162;
    weight[2] = 0.0702702703;

    vec2 currentCoord = vec2(gl_FragCoord);

    vec4 fragmentColor = texture2D(u_texture, currentCoord / u_size) * weight[0];
    for (int i = 1; i < 3; i++) {
       fragmentColor +=
           texture2D(u_texture, (currentCoord+vec2(0.0, offset[i])) / u_size) * weight[i];
       fragmentColor +=
           texture2D(u_texture, (currentCoord-vec2(0.0, offset[i])) / u_size) * weight[i];
    }

    return fragmentColor;
}

void main() {

    // Apply a Gaussian blur to the FBO texture
    vec4 result = gaussianBlur();

    // If the blurred pixel is not white enough, discard it
    if (result.r < LOWER_COLOR_CUTOFF) discard;

    if (result.r < ALPHA_CUTOFF) {
        result.a = (result.r / ALPHA_CUTOFF) * 0.75;
    } else {
        result.a = 0.75;
    }

    if (result.r < UPPER_COLOR_CUTOFF) {
        result.rgb = vec3(0.659, 0.871, 0.941);
    } else {
        result.rgb = vec3(0.251, 0.729, 0.890);
    }

    gl_FragColor = result;
}