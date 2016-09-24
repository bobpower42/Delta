#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform vec2 texOffset;
uniform float rgbOffset = 0.0004;
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
	float red = texture2D(texture,uvR).r;
	float green = texture2D(texture,uv).g;
	float blue = texture2D(texture,uvB).b;	
	vec3 color = vec3(red,green,blue);	
	color = color*fade;	
	gl_FragColor = vec4(color,0.8);
}
