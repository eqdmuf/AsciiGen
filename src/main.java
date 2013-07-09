import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;

/**
 * A little driver for the Ascii generator with a simple CLI.
 */
public class main{

  static final boolean nix=System.getProperty("os.name").endsWith("x");
  static final int TXT = 1, IMG =2, BOTH = 3;
  static boolean verbose=true;
  static boolean displayImg=false, sysDisplayImg=false, writeImg=true;
  static boolean invert=false,imgColor=false,saturated,bright,vertical;
  static double sFactor=.5, blackThresh=-1;
  static String in, out, outTxt, palette, phrase, fontName;
  static int paletteNum;
  static int outputMode;
  static int index=0;
  static Rectangle srcRect, destRect;
  static Color fg, bg;
  static BufferedImage dest;
  static Ascii ascii;

  public static void main(String args[]){
    parseArgs(args);
    setDefaults();
    outf("Reading %s\n",in);
    BufferedImage srcUnscaled=getImage(in);
    final BufferedImage src=scale(srcUnscaled,null,2*sFactor,1*sFactor);
    int w=src.getWidth(), h=src.getHeight();

    outf("Finished scaling to size: %dx%d\n",w,h);

    if(palette==null) palette=Ascii.getPalette(paletteNum);
    if(invert) palette=new StringBuilder(palette).reverse().toString();
    ascii=new Ascii(src,palette);
    configureAscii();
    outf("Using palette \"%s\"\n",ascii.palette);

    //Text output
    if((outputMode&TXT)!=0){
      try{
	final PrintStream p=new PrintStream(new File(outTxt));
	outf("Writing to %s...",outTxt);
	ascii.displayAscii(p,0,0,w,h);
	p.close();
	outn("Done");
      } catch(FileNotFoundException fne){outn("Failed");}
    }
    //Image output
    if((outputMode&IMG)!=0){
      outf("Creating dest image of size %dx%d\n",w*4,h*8);
      dest=createImage(w*4, h*8);//todo better sizing bc of x croppping
      outn("Rendering ascii to dest "+out);
      setRects();
      ascii.displayAscii(dest.getGraphics(),srcRect,destRect);
      if(writeImg){
	try{
	  outf("Writing image to file %s...",out);
	  ImageIO.write(dest,out.substring(out.lastIndexOf(".")+1),new File(out));
	  outn("Done");
	}
	catch(IOException e){
	  outn("Failed");
	  //e.printStackTrace();
	}
      }
      if(displayImg || sysDisplayImg)
	displayImage(dest, 900,700);
    }
  }

  public static void parseArgs(String[] args){
    for(index=0;index<args.length;++index){
      String s=args[index];
      if(s.charAt(0)!='-'){
	in=s;
	continue;
      }
      s=s.substring(1);
      if(equals(s,"-")){
	break;
      }
      else if(equals(s,"FLAGS")){
	//do nothing
      }
      else if(equals(s,"p")){
	palette=args[++index];
      }
      else if(equals(s,"o")){
	out=args[++index];
      }
      else if(s.startsWith("p=")){
	paletteNum=parseInt(s.substring(2));
      }
      else if(equals(s,"ph")){
	phrase=args[++index];
      }
      else if(equals(s,"t")){
	outTxt=args[++index];
      }
      else if(equals(s,"di")){
	displayImg=true;//Boolean.parseBoolean(args[++index]);
      }
      else if(equals(s,"sdi")){
	sysDisplayImg=true;
      }
      else if(equals(s,"nwi")){
	writeImg=false;
      }
      else if(equals(s,"wi")){
	writeImg=Boolean.parseBoolean(args[++index]);
      }
      else if(equals(s,"v")){
	verbose=true;
      } 
      else if(equals(s,"nv") || equals(s,"q")){
	verbose=false;
      }
      else if(equals(s,"s") || equals(s,"scale")){
	sFactor=Double.parseDouble(args[++index]);
      } 
      else if(equals(s,"w")){
	String w=args[++index];
	if(w.charAt(0)=='-') w=w.substring(1);
	if(w.equals("t") || equals(s,"txt")){
	  outputMode=outputMode|TXT;
	}
	else if(w.equals("i") || w.equals("img")){
	  outputMode=outputMode|IMG;
	}
	else if(w.equals("b") || w.equals("both")){
	  outputMode=outputMode|BOTH;
	}
      }
      else if(equals(s,"vert") || equals(s,"vertical")){
	vertical=true;
      }
      else if(equals(s,"horiz") || equals(s,"horizontal")){
	vertical=false;
      }
      else if(equals(s,"flip") || equals(s,"-flip-palette")){
	invert=true;
      }
      else if(equals(s,"imc") || equals(s,"-image-color")){
	imgColor=true;
      }
      else if(equals(s,"sat") || equals(s,"-saturate")){
	saturated=true;
      }
      else if(equals(s,"bri") || equals(s,"-bright")){
	bright=true;
      }
      else if(equals(s,"fg") || equals(s,"-foreground")){
	fg=parseCol(args[++index]);
      }
      else if(equals(s,"bg") || equals(s,"-background")){
	bg=parseCol(args[++index]);
      }
      else if(equals(s,"mat") || equals(s,"-matrix")){
	fg=Color.green;
	bg=Color.black;
      }
      else if(equals(s,"xir") || equals(s,"-xirtam")){
	bg=Color.green;
	fg=Color.black;
      }
      else if(equals(s,"f") || equals(s,"font")){
	fontName=args[++index];
      }
      else if(s.startsWith("black=")){
	blackThresh=parseDouble(s.substring(6));
      }
      else{
	System.err.println("Unrecognized flag: \""+s+"\"");
	System.exit(2);
      }

    }//end for
  }

  private static void setDefaults(){
    if(outputMode==0){
      outputMode=IMG;
    }
    if(in==null){
      in="";//todo impl image filter and sel
    }
    if(out==null){
      out="Ascii"+in;
    }
    if(outTxt==null){
      outTxt=in+".ascii";
    }
    if(!(writeImg || displayImg) && (outputMode&IMG)!=0){
      errn("Not writing or displaying rendered image");
      if(outputMode==IMG){//only outputting image
	errn("\tSince you're only outputting an image, I'm going display it");
	displayImg=true;
      } else{
	errn("\tI'm not gonna bother with it then");
	outputMode=outputMode^IMG;
      }
    }

  }
  public static void setRects(){
    srcRect=ascii.getBounds();
    destRect=getBounds(dest);
  }
  public static void configureAscii(){
    ascii.phrase=phrase;
    ascii.useImgColor=imgColor;
    ascii.saturated=saturated;
    ascii.bright=bright;
    ascii.vertical=vertical;
    if(imgColor)
      outn("Using image's colors for text foreground");
    setColors();
  }
  public static void setColors(){
    if(fg!=null){
      ascii.fg=fg;
      outf("Setting foreground to %h\n",fg.getRGB());
    }
    if(bg!=null){
      ascii.bg=bg;
      outf("Setting background to %h\n",bg.getRGB());
    }
    if(blackThresh!=-1){
      ascii.blackThresh=blackThresh;
    }

  }
  private static void out(String s){
    if(verbose) System.out.print(s);
  }
  private static void outn(String s){
    if(verbose) System.out.println(s);
  }

  private static void outf(String s, Object... args){
    if(verbose) System.out.printf(s,args);
  }
  private static void errn(String s){
    if(verbose) System.err.println(s);
  }
  private static BufferedImage getImage(String filename){
    try{
      return ImageIO.read(new File(filename));
    }
    catch(IOException e){
      System.out.println(filename);
      e.printStackTrace();
      return null;
    }
  }
  private static BufferedImage createImage(int w, int h){
    return
      GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().
      getDefaultConfiguration().createCompatibleImage(w,h);
  }
  private static BufferedImage scale(BufferedImage src, BufferedImage dest, double sx, double sy){
    AffineTransform tx = new AffineTransform();
    tx.scale(sx,sy);
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
    dest = op.filter(src, dest);
    return dest;
  }
  private static Rectangle getBounds(final BufferedImage i){
    return new Rectangle(i.getMinX(),i.getMinY(),i.getWidth(),i.getHeight());
  }
  private static void displayImage(final Image img,final int w,final int h){
    if(sysDisplayImg && writeImg){
      try{sysOpen(out); return;}
      catch(Exception e){
	errn("Failed to sys-open image "+out);	
      }
    }
    new ImgPreview(img,w,h).setVisible(true);
  }
  static boolean equals(String a, String b){
    return a.equals(b);
  }
  static int parseInt(String s){
    return parseInt(s,10);
  }
  static double parseDouble(String s){
    return Double.parseDouble(s.trim());
  }
  static int parseInt(String s, int rad){
    return Integer.parseInt(s.trim(),rad);
  }
  static Color parseCol(String s){
    if(s.contains(",")){
      String c[]=s.split(",");
      return new Color(parseInt(c[0]),parseInt(c[1]),parseInt(c[2]));
    }
    return new Color(parseInt(s,16),false);
  }
  static void sysOpen(String out) throws IOException{
    String[] opens=(nix)?nixOpens:winOpens;
    Runtime r=Runtime.getRuntime();
    for(String prog:opens){//todo fix crappy handling
      outn("executing "+prog+" "+out);
      r.exec(String.format("%s %s",prog,out));
    }
  }
  static class ImgPreview extends JFrame{
    Image img;
    public ImgPreview(Image img, int w, int h){
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setSize(w,h);
      this.img=img;
    }
    public void paint(Graphics g){
      g.drawImage(img,0,0,getWidth(),getHeight(),this);
    }	
  }

  static final String[] nixOpens={"gnome-open"};//,"kde-open"};
  static final String[] winOpens={"open"};
}
