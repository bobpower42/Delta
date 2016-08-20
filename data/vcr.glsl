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
uniform float magX=1.0;
uniform float magY=1.0;

varying vec4 vertColor;
varying vec4 vertTexCoord;



void main(void) {

	vec2 uv=vertTexCoord.xy;
	vec2 offset=vec2(rgbOffset*magX,rgbOffset*magY);
	vec2 uvR=uv-offset;
	vec2 uvB=uv+offset;
	 
   
	float red = texture2D(texture,uvR).r+texture2D(ppixels,uvR).r+texture2D(texture,vec2(texOffset.s+uvR.x,uvR.y)).r+texture2D(texture,vec2(-texOffset.s+uvR.x,uvR.y)).r;
	float green = texture2D(texture,uv).g+texture2D(ppixels,uv).g+texture2D(texture,vec2(texOffset.s+uv.x ,uv.y)).g+texture2D(texture,vec2(-texOffset.s+uv.x ,uv.y)).g;
	float blue = texture2D(texture,uvB).b+texture2D(ppixels,uvB).b+texture2D(texture,vec2(texOffset.s+uvB.x ,uvB.y)).b+texture2D(texture,vec2(-texOffset.s+uvB.x ,uvB.y)).b;
	
	//float red = 3*texture2D(texture,vec2(uv.x-rgbOffset*magX,uv.y-rgbOffset*magY)).r+texture2D(ppixels,vec2(uv.x-rgbOffset*magX,uv.y-rgbOffset*magY)).r;
	//float green = 3*texture2D(texture,vec2(uv.x ,uv.y)).g+texture2D(ppixels,vec2(uv.x ,uv.y)).g;
	//float blue = 3*texture2D(texture,vec2(uv.x+rgbOffset*magX,uv.y+rgbOffset*magY)).b+texture2D(ppixels,vec2(uv.x+rgbOffset*magX,uv.y+rgbOffset*magY)).b;
	
	
	
	vec3 color = vec3(red/4,green/4,blue/4);
	if(scanlines!=0){
	float scanline = sin(uv.y*scanlinesNum)*0.02*scanlines;
	color -= scanline;
	}
	
	gl_FragColor = vec4(color,1.0);
}
