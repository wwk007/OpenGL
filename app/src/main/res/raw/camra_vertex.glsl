attribute vec4 vPosition;
attribute vec2 aCoord;
uniform mat4 vMatrix;
varying vec2 vCoord;
void main() {
    gl_Position = vMatrix*vPosition;
    vCoord = aCoord;
}