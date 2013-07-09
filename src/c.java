public class c{
  static int cols=8;
  public static void main(String... args){
    char c=0x2588;
    System.out.println("\'"+c+"\'");
    System.out.println(c+""+c);
  }
  public static void main1(String... args){
    java.io.PrintStream out=System.out;
    int end=args.length==0?512:Integer.parseInt(args[0]);
    //String format="%"+(end+"").length()+"d:\'%c\'\t";
    //String format="%-"+(h(end)).length()+"s:\'%c\'\t";
    String format="%s:\'%c\'\t";
    for(int i=1;i<end;++i){
      out.printf(format,h(i),(char)i);
      if(i%8==0) out.println();
    }
  }
  public static String h(int i){
    return Integer.toHexString(i).trim();
  }
}
