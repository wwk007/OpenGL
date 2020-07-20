#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES  vTexture;
varying vec2 vCoord;
void main() {
  gl_FragColor = texture2D(vTexture,vCoord);
}
