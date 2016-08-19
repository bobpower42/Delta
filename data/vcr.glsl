#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform vec2 texOffset;
uniform float scanlines = 1.0;
uniform float rgbOffset = 0.0005;
uniform float magX=1.0;
uniform float magY=1.0;

varying vec4 vertColor;
varying vec4 vertTexCoord;



void main(void) {

vec2 uv =  vertTexCoord.xy;
	 
   
	float red = texture2D(texture,vec2(uv.x-rgbOffset*magX,uv.y-rgbOffset*magY)).r+texture2D(texture,vec2(-texOffset.s+uv.x-rgbOffset*magX,uv.y-rgbOffset*magY)).r+texture2D(texture,vec2(-2*texOffset.s+uv.x-rgbOffset*magX,uv.y-rgbOffset*magY)).r;
	float green = texture2D(texture,vec2(uv.x ,uv.y)).g+texture2D(texture,vec2(-texOffset.s+uv.x ,uv.y)).g+texture2D(texture,vec2(-2*texOffset.s+uv.x ,uv.y)).g;
	float blue = texture2D(texture,vec2(uv.x+rgbOffset*magX,uv.y+rgbOffset*magY)).b+texture2D(texture,vec2(-texOffset.s+uv.x+rgbOffset*magX,uv.y+rgbOffset*magY)).b+texture2D(texture,vec2(-2*texOffset.s+uv.x+rgbOffset*magX,uv.y+rgbOffset*magY)).b;
	
	vec3 color = vec3(red/3.0,green/3.0,blue/3.0);
	if(scanlines!=0){
	float scanline = sin(uv.y*1600.0)*0.02*scanlines;
	color -= scanline;
	}
	
	gl_FragColor = vec4(color,1.0);
}
