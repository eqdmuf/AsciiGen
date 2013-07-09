Ascii "art" generator
This is just a slapdash Ascii generator I made that follows the simplistic
approach of computing pixel brightness and mapping that [0:1] value to
some character that (hopefully) will reproduce that level of brightness
in the output.

Some things could really improve on this, e.g. edge-detection, non-linear
mapping to characters, self-computing palettes, better interpolation,
data manipulation, image processing (blurring might be useful), etc.

The main.java implements a simple, undocumented CLI to the Ascii class. 
By any Java coding standards, it's terrible, but it somewhat works for me.

The project is compiled by calling make which will output a jar into '.'
and the 3 class files in build/.

The system opening of images is not portable at all, only using GNOME's 
"gnome-open" or Window's "open", so change it by hand if you want that
functionality (which beats the crappy litle ImgPreview gui).
