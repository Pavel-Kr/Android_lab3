uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
attribute vec2 a_TexCoords;
varying vec2 v_TexCoords;
void main() {
    gl_Position = uMVPMatrix * vPosition;
    v_TexCoords = a_TexCoords;
}