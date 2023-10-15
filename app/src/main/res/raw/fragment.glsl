precision mediump float;
uniform vec4 vColor;
uniform sampler2D u_TextureUnit;
varying vec2 v_TexCoords;
void main() {
  gl_FragColor = texture2D(u_TextureUnit, v_TexCoords);
}