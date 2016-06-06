#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {
    if (texture2D(u_texture, v_texCoord).rgb == vec3(0, 0, 0)) {
        discard;
    } else {
        gl_FragColor = vec4(0, 0, 1, 0.5);
    }
}