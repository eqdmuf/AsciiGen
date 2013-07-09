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
  static boolean invert=false;
  static double sFactor=.5;
  static String in, out, outTxt, palette, fontName;
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
      dest=createImage(w*4, h*8);
      outn("Rendering ascii to dest "+out);
      setRects();
      setColors();
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
      if(s.equals("p")){
	palette=args[++index];
      }
      else if(s.startsWith("p=")){
	paletteNum=Integer.parseInt(s.substring(2));
      }
      else if(s.equals("o")){
	out=args[++index];
      }
      else if(s.equals("t")){
	outTxt=args[++index];
      }
      else if(s.equals("di")){
	displayImg=true;//Boolean.parseBoolean(args[++index]);
      }
      else if(s.equals("sdi")){
	sysDisplayImg=true;
      }
      else if(s.equals("nwi")){
	writeImg=false;
      }
      else if(s.equals("wi")){
	writeImg=Boolean.parseBoolean(args[++index]);
      }
      else if(s.equals("v")){
	verbose=true;
      } 
      else if(s.equals("nv")){
	verbose=false;
      }
      else if(s.equals("s")){
	sFactor=Double.parseDouble(args[++index]);
      } 
      else if(s.equals("w")){
	String w=args[++index];
	if(w.charAt(0)=='-') w=w.substring(1);
	if(w.equals("t")){
	  outputMode=outputMode|TXT;
	}
	else if(w.equals("i")){
	  outputMode=outputMode|IMG;
	}
	else if(w.equals("b")){
	  outputMode=outputMode|BOTH;
	}
      
      }
      else if(s.equals("inv")){
	invert=true;
      }
      else if(s.equals("fg")){
	fg=parseCol(args[++index]);
      }
      else if(s.equals("bg")){
	bg=parseCol(args[++index]);
      }
      else if(s.equals("matrix")){
	fg=Color.green;
	bg=Color.black;
      }
      else if(s.equals("xirtam")){
	bg=Color.green;
	fg=Color.black;
      }
      else if(s.equals("f")){
	fontName=args[++index];
      }
      else{
	System.err.println("Unrecognized flag: "+s);
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
  public static void setColors(){
    if(fg!=null){
      ascii.fg=fg;
      outf("Setting foreground to %h\n",fg.getRGB());
    }
    if(bg!=null){
      ascii.bg=bg;
      outf("Setting background to %h\n",bg.getRGB());
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
  
  static int parseInt(String s){
    return Integer.parseInt(s);
  }
  static Color parseCol(String s){
    if(s.contains(",")){
      String c[]=s.split(",");
      return new Color(parseInt(c[0]),parseInt(c[1]),parseInt(c[2]));
    }
    return new Color(Integer.parseInt(s,16),false);
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
