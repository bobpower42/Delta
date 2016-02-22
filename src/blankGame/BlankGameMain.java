package blankGame;

import processing.core.PApplet;


public class BlankGameMain extends PApplet {

    public static void main(String[] args) {
        String[] a = {"MAIN"};
        PApplet.runSketch( a, new BlankGameMain());
    } 
         
    public void settings(){
        fullScreen(P2D); 
        smooth();        
    }
 
    public void setup() {    	
    	frameRate(60);    	
    }
 
    
 
    public void draw() {
        
    }    
}

