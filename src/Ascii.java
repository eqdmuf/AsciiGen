import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;


public class Ascii{

  BufferedImage image;
  String palette, phrase;
  int scale, index, alpha=0xff;
  Color bg=Color.white, fg=Color.black;
  Font font=new Font("Arial Monospaced",Font.BOLD,6);  
  boolean useImgColor, saturated, bright, vertical;
  double blackThresh=.01;
  public Ascii(BufferedImage im){
    this(im,palette0);
  }
  public Ascii(BufferedImage im, int paletteNum){
    this(im,palettes[paletteNum]);
  }
  public Ascii(BufferedImage im, String palette){
    image=im;
    setPalette(palette);
  }
  
  public int getWidth(){
    return image.getWidth();
  }
  public int getHeight(){
    return image.getHeight();
  }
  public String palete(){
    return palette;
  }
  public Font font(){
    return font;
  }
  public Color bakground(){
    return bg;
  }
  public Color foreground(){
    return fg;
  }
  public String palete(String s){
    return palette=s;
  }
  public Font font(Font f){
    return font=f;
  }
  public Color bakground(Color c){
    return bg=c;
  }
  public Color foreground(Color c){
    return fg=c;
  }

  public double r(double x, double y){
    int X=(int)x, Y=(int) y;
    double b11=r(X,Y);
    double b21=r(X+1,Y);
    double b22=r(X+1,Y+1);
    double b12=r(X,Y+1);
    return lerp(lerp(b11,b21,x-X),lerp(b21,b22,x-X),y-Y);
  }
  
  public Color setAlpha(Color c){
    return new Color(c.getRGB()&0x00ffffff|(alpha<<24),true);
  }

  public Color getColor(double x, double y){
    int X=(int)x, Y=(int) y;
    int[] col=new int[3];
    int[] c11=getRGB(X,Y);
    int[] c21=getRGB(X+1,Y);
    int[] c22=getRGB(X+1,Y+1);
    int[] c12=getRGB(X,Y+1);
    for(int i=0;i<3;++i){
       col[i]=(int)(lerp(lerp(c11[i],c21[i],x-X),lerp(c21[i],c22[i],x-X),y-Y)+.5);
    }
    //System.out.println(java.util.Arrays.toString(col));
    return getTextColor(col[0],col[1],col[2]);
  }

  public Color getTextColor(int r, int g, int b){
    float[] hsb=Color.RGBtoHSB(r,g,b,null);
    return Color.getHSBColor(hsb[0],saturated?1f:hsb[1],bright?1f:hsb[2]);
  }
  
  public int[] getRGB(int x, int y){
    if(x>=getWidth() || y>=getHeight())
      return new int[]{0,0,0};
    int clr = image.getRGB(x,y);
    int r   = (clr & 0x00ff0000) >> 16;
    int g = (clr & 0x0000ff00) >> 8;
    int b  =  clr & 0x000000ff;
    return new int[]{r,g,b}; 
  }
  
  public int[] getRGBA(int x, int y){
    if(x>=getWidth() || y>=getHeight())
      return new int[]{0,0,0,0};
    int clr = image.getRGB(x,y);
    int a=(clr & 0xff000000) >> 24;
    int r   = (clr & 0x00ff0000) >> 16;
    int g = (clr & 0x0000ff00) >> 8;
    int b  =  clr & 0x000000ff;
    return new int[]{r,g,b,a }; 
  }

  public double r(int x, int y){
    //return (image.getRGB(x,y) & 0x00ff0000) >> 16;
    if(x>=getWidth() || y>=getHeight())
      return 0;
    int[] rgb=getRGB(x,y);
    return getBrightness(rgb[0],rgb[1],rgb[2]);

  }
  void setPalette(String p){
    palette=p;
    scale=p.length()-1;
  }
  void setPalette(int i){
    setPalette(palettes[i]);
  }
  public char getCharFor(double r){//in monochrome r=g=b
    return palette.charAt((int) (r*scale));
  }
  public char getCharAt(int x, int y){
    return getCharFor(r(x,y));
  }
  public char getCharAt(double x, double y){
    return getCharFor(r(x,y));
  }
  public void displayAscii(){
    displayAscii(System.out,0,0,getWidth(),getHeight());
  }
  public void displayAscii(int w, int h){
    displayAscii(System.out,0,0,w,h);
  }
  public void displayAscii(PrintStream p, int xOff, int yOff, int w, int h){
    int x,y;
    int endX=xOff+w;
    int endY=yOff+h;
    char c;
    StringBuilder sb=new StringBuilder(w*h);
    for(y=yOff;y!=endY;y++){
      for(x=xOff;x!=endX;x++){
	c=getCharAt(x,y);
	sb=sb.append(c);
      }
      p.println(sb.toString());
    }
  }
  public void displayAsciiMono(Graphics g,int xOff, int yOff, int w, int h){
    g.setFont(font);
    FontMetrics fm=g.getFontMetrics();
    g.setColor(bg);
    g.fillRect(0,0,w*fm.getMaxAdvance(),h*fm.getHeight());
    g.setColor(setAlpha(fg));
    int xPixel=0,yPixel=0, x,y;
    int endX=xOff+w, endY=yOff+h;
    for(y=yOff;y!=endY;y++){
      xPixel=0;
      yPixel+=fm.getHeight();
      for(x=xOff;x!=endX;x++){
	char c=getCharAt(x,y);
	if(useImgColor)
	  g.setColor(getColor(x,y));
	g.drawString(""+c,xPixel,yPixel);
	xPixel+=fm.charWidth(c);
      }
    }
  }
  public void displayAscii(Graphics g, Rectangle src, Rectangle dest){
    g.setFont(font);
    FontMetrics fm=g.getFontMetrics();
    if(alpha==0xff){
      g.setColor(bg);
      g.fillRect(dest.x,dest.y,src.width*fm.getMaxAdvance(),src.height*fm.getHeight());
    }
    g.setColor(setAlpha(fg));
    if(vertical){
      loopDisplayAsciiV(g,src,dest);
    } else{
      loopDisplayAsciiH(g,src,dest);
    }
  }
  public void loopDisplayAsciiH(Graphics g,Rectangle src, Rectangle dest){
    FontMetrics fm=g.getFontMetrics();
    int yPixel=0, xPixel;
    double x, xScale=src.width/(double)dest.width;
    for(int y=0;y<src.height;y++){
      yPixel+=fm.getHeight();
      x=xPixel=0;
      do{
	//System.out.printf("reading %d/,%d %f/\n",y,xPixel,x);
	if(useImgColor)
	  g.setColor(setAlpha(getColor(x,y)));
	char c=(phrase==null)?getCharAt(x+src.x,y+src.y):
	  getPhraseChar(g.getColor());
	g.drawString(""+c,dest.x+xPixel,dest.y+yPixel);
	xPixel+=fm.charWidth(c);
	x=xPixel*xScale;
      }while(x<src.width);

    }

  }
  public void loopDisplayAsciiV(Graphics g,Rectangle src, Rectangle dest){
    FontMetrics fm=g.getFontMetrics();
    int yPixel=0, xPixel=0;
    double y, yScale=src.height/(double)dest.height;
    int charW=getMaxAdvance(fm);
    for(int x=0;x<src.width;++x){
      xPixel+=charW;
      y=yPixel=0;
      do{
	//System.out.printf("reading %d/,%d %f/\n",y,xPixel,x);
	if(useImgColor)
	  g.setColor(setAlpha(getColor(x,y)));
	char c=(phrase==null)?getCharAt(x+src.x,y+src.y):
	  getPhraseChar(g.getColor());
	g.drawString(""+c,dest.x+xPixel,dest.y+yPixel);
	yPixel+=fm.getHeight();
	y=yPixel*yScale;
      }while(y<src.height);
    }

  }
  public char getPhraseChar(Color c){
    char ch=phrase.charAt(index);
    if(!isBlack(c))
      index=(index+1)%phrase.length();
    return ch;

  }
  public String toString(){
    StringBuilder sb=new StringBuilder();
    int x,y;
    for(y=0;y!=getHeight();y++){
      for(x=0;x!=getWidth();x++){
	sb.append(getCharAt(x,y));
      }
    }
    return sb.toString();
  }
  public Rectangle getBounds(){
    return new Rectangle(0,0,getWidth(),getHeight());
  }
  public double getBrightness(int r, int g, int b){
    return smooth(brightness(r,g,b));
  }
  public double smooth(double x){
    return (x);
  }
  public static double brightness(int r, int g, int b){
    return (r*BR_R+b*BR_B+g*BR_G)/255;
  }
  public static double cos(double x){
    return (1-Math.cos(x*Math.PI))/2;
  }
  public static double quad(double a){
    return a*a;
  }
  public static double lerp(double a, double b, double x){
    return b*x+a*(1-x);
  }
  public static String getPalette(int num){
    return palettes[num];
  }

  public boolean isBlack(Color c){
    return getBrightness(c.getRed(),c.getGreen(),c.getBlue())<blackThresh;
  }
  public int getMaxAdvance(FontMetrics fm){
    if(phrase==null)
      return fm.getMaxAdvance();
    int m=0;
    for(int i=0;i<phrase.length();++i){
      m=Math.max(m,fm.charWidth(phrase.charAt(i)));
    }
    return m;
  }
  static double BR_R=.3, BR_B=.59, BR_G=.11;

  //static final boolean nix=System.getProperty("os.name").contains("nix");
  //static final char CURSOR_BLACK=(nix?(char)0x2588:219);
  //static final char CURSOR_DARK=(nix?(char)0x2593: 178);
  //static final char CURSOR_GRAY=(nix?(char)0x2592:177);
  //static final char CURSOR_LIGHT=(nix?(char)0x2591:176);
  //static final char DOT=(nix?(char)0x25E6:250);
  //static final char BLACK_SMILEY=(nix?(char)0x263B:2);
  //static final boolean nix=System.getProperty("os.name").contains("nix");
  static final char CURSOR_BLACK=0x2588;
  static final char CURSOR_DARK=0x2593;
  static final char CURSOR_GRAY=0x2592;
  static final char CURSOR_LIGHT=0x2591;
  static final char DOT=0x25E6;
  static final char BLACK_SMILEY=0x263B;
  static final String palette0="@#$&%8BMOmkhaoX+=_~-;|\"\',."+DOT+"    ";
  static final String palette1=""+CURSOR_BLACK+BLACK_SMILEY+CURSOR_DARK+"@"+CURSOR_GRAY+"#$&%8BMOmkhaoX+=_~-;|\"\',."+DOT+"    ";
  static final String palette2=""+CURSOR_BLACK+BLACK_SMILEY+CURSOR_DARK+"@"+CURSOR_GRAY+"#O;.   ";
  static final String palette3="@#$%&8BMW*mwqpdbkhaoQ0OZXYUJCLtfjzxnuvcr[]{}1()|/?Il!i><+_~-;,. ";
  static final String
    palette4=""+CURSOR_BLACK+BLACK_SMILEY+""+CURSOR_DARK+"@"+CURSOR_GRAY+"#$%&8BMW*mwqpdbkhaoQ0OZXYUJCLtfjzxnuvcr[]{}1()|/?Il!i><+_~-;,."+DOT+"     ";
  static final String palettes[]={palette0,palette1,palette2,palette3,palette4};
}
