import engine.*;
import screen.*;
import records.*;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

/** Deterministic full-buffer captures to compare with virtual X11 window grabs. */
public class RecordsBufferCapture {
    public static void main(String[] args) throws Exception {
        Path output=Paths.get(args[0]);Files.createDirectories(output);
        RecordsService service=new RecordsService(output.resolve("empty-profile.properties"));
        Screen screen=new Screen(448,520,60);DrawManager draw=Core.getDrawManager();
        Field buffer=DrawManager.class.getDeclaredField("backBuffer");buffer.setAccessible(true);
        try {
            for(int tab=0;tab<2;tab++) {
                draw.initDrawing(screen);draw.drawRecordsPage(screen,service,tab);
                draw.completeDrawing(screen);
                ImageIO.write((BufferedImage)buffer.get(null),"png",output.resolve(tab==0?"records-empty-buffer.png":"achievements-locked-buffer.png").toFile());
            }
        } finally {service.close();}
        System.out.println("Full-buffer captures completed: "+output);
    }
}
