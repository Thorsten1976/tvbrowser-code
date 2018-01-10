package tvbrowserdataservice.file;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import util.io.ExecutionHandler;
import util.io.IOUtilities;

/**
 * Helper class to load pictures.
 * 
 * @author René Mach
 * @since 3.1.2
 */
public class PictureUtils {
  /** The maximum width/height of a picture */
  public static final int MAX_SIZE = 300;
  
  /**
   * Loads a picture to a BufferedImage, if a picture cannot be load by Java 
   * and a ImageMagick installation exists at /usr/bin/convert it is tried to
   * convert the picture to RGB and to load the converted file.
   * 
   * @param pictureFile The picture file at the file system.
   * @return The BufferedImage that was load from the picture file.
   * @throws IOException Thrown if the picture could not be load.
   */
  public static final BufferedImage loadImageCompatible(final File pictureFile) throws IOException {
    BufferedImage result = null;
    InputStream in = null;
    
    try {
      in = new FileInputStream(pictureFile);
      result = loadImageCompatible(in, pictureFile.getName());
    }catch(IOException ioe) {
      throw ioe;
    }finally {
      if(in != null) {
        try {
          in.close();
        }catch(IOException ioe2) {}
      }
    }
    
    return result;
  }
  
  /**
   * Loads a picture to a BufferedImage, if a picture cannot be load by Java 
   * and a ImageMagick installation exists at /usr/bin/convert it is tried to
   * convert the picture to RGB and to load the converted file.
   * 
   * @param in The input stream of the picture file.
   * @param fileName The file name of the picture file without path.
   * @return The BufferedImage that was load from the picture file.
   * @throws IOException Thrown if the picture could not be load.
   */
  public static final BufferedImage loadImageCompatible(final InputStream in) throws IOException {
    return loadImageCompatible(in, String.valueOf(Math.random()*1000) + "_" + String.valueOf(Math.random()*1000) + "_" +  String.valueOf(Math.random()*1000)+".jpg");
  }
  
  /**
   * Loads a picture to a BufferedImage, if a picture cannot be load by Java 
   * and a ImageMagick installation exists at /usr/bin/convert it is tried to
   * convert the picture to RGB and to load the converted file.
   * 
   * @param in The input stream of the picture file.
   * @param fileName The file name of the picture file without path.
   * @return The BufferedImage that was load from the picture file.
   * @throws IOException Thrown if the picture could not be load.
   */
  public static final BufferedImage loadImageCompatible(final InputStream in, String fileName) throws IOException {
    final File iMagick = new File("/usr/bin/convert");
    
    BufferedImage result = null;
    IOException toThrow = null;
    
    BufferedInputStream temp = null;
    
    try {
      temp = new BufferedInputStream(in);
      temp.mark(1024 * 1024 * 30);
      
      try {
        result = ImageIO.read(temp);
      }catch(IOException ioe) {
        toThrow = ioe;
      }
      
      if(toThrow != null) {
        temp.reset();
        
        if(iMagick.isFile()) {
          if(fileName.indexOf("/") != -1) {
            fileName = fileName.substring(fileName.lastIndexOf("/")+1);
          }
          
          final File target = new File(System.getProperty("java.io.tmpdir","/tmp"),"_"+fileName);
          
          FileOutputStream out = null;
          
          try {
            out = new FileOutputStream(target);
            
            IOUtilities.pipeStreams(temp, out);
          }catch(IOException ioe2) {
            ioe2.printStackTrace();
            throw toThrow;
          } finally {
            if(out != null) {
              try {
                out.flush();
              }catch(IOException ioe3) {}
              try {
                out.close();
              }catch(IOException ioe3) {}
            }
          }
          
          if(target.isFile()) {
            final File targetOut = new File(target.getParentFile(),"_rgb_"+target.getName());
            
            final ExecutionHandler exec = new ExecutionHandler(new String[]{iMagick.getAbsolutePath(),"-colorspace","rgb","-quality","100",target.getAbsolutePath(),targetOut.getAbsolutePath()}, "/tmp");
            
            try {
              exec.execute();
              exec.getProcess().waitFor();
              
              result = ImageIO.read(targetOut);
            }catch(Exception ioe4) {
              throw toThrow;
            }finally {
              if(!target.delete()) {
                target.deleteOnExit();
              }
              if(targetOut.isFile() && !targetOut.delete()) {
                targetOut.deleteOnExit();
              }
            }
          }
        }
        else {
          throw toThrow;
        }
      }
    }catch(IOException ioe6) {
      throw ioe6;
    }finally {
      if(temp != null) {
        try {
          temp.close();
        }catch(IOException ioe7) {}
      }
    }
    
    return result;
  }
}
