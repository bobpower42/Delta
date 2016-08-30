#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform sampler2D ppixels;
uniform vec2 texOffset;
uniform float scanlines = 0.5;
uniform float scanlinesNum=800.0;
uniform float rgbOffset = 0.0007;
uniform float fade=0.0;
uniform float magX=1.0;
uniform float magY=1.0;

varying vec4 vertColor;
varying vec4 vertTexCoord;



void main(void) {
	vec2 uv=vertTexCoord.xy;
	vec2 offset=vec2(rgbOffset*magX,rgbOffset*magY);
	vec2 uvR=uv-offset;
	vec2 uvB=uv+offset;   
	float red = 3*texture2D(texture,uvR).r+texture2D(ppixels,uvR).r;
	float green = 3*texture2D(texture,uv).g+texture2D(ppixels,uv).g;
	float blue = 3*texture2D(texture,uvB).b+texture2D(ppixels,uvB).b;	
	vec3 color = vec3(red*0.25,green*0.25,blue*0.25);	
	float scanline = sin(uv.y*scanlinesNum)*0.02*scanlines;
	color = color*fade-scanline;	
	gl_FragColor = vec4(color,1.0);
}
