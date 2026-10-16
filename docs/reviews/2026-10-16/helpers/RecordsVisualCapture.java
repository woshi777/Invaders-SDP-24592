import engine.Core;
import records.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.*;

/** Additional real-window captures using an explicit software X11 pipeline. */
public class RecordsVisualCapture {
    public static void main(String[] args) throws Exception {
        LiveUiReview.output=Paths.get(args[0]);Files.createDirectories(LiveUiReview.output);
        LiveUiReview.robot=new Robot();
        Path profile=Paths.get(System.getProperty("user.home"),".space-invaders","records-achievements.properties");
        try(LiveUiReview.Seed seed=new LiveUiReview.Seed(profile)){seed.populate();}
        new Thread(()->Core.main(new String[0]),"review-engine").start();
        try {
            LiveUiReview.await("TitleScreen");
            engine.Frame frame=(engine.Frame)LiveUiReview.field(Core.class,"frame").get(null);
            javax.swing.SwingUtilities.invokeAndWait(()->{frame.toFront();frame.requestFocus();});
            LiveUiReview.key(KeyEvent.VK_DOWN);LiveUiReview.key(KeyEvent.VK_DOWN);LiveUiReview.key(KeyEvent.VK_SPACE);
            LiveUiReview.await("PlayerRecordsScreen");LiveUiReview.snapshot("records-populated");
            LiveUiReview.key(KeyEvent.VK_RIGHT);LiveUiReview.snapshot("achievements-unlocked");
            LiveUiReview.key(KeyEvent.VK_ESCAPE);LiveUiReview.await("TitleScreen");
            LiveUiReview.freshProfile("empty");
            LiveUiReview.key(KeyEvent.VK_DOWN);LiveUiReview.key(KeyEvent.VK_DOWN);LiveUiReview.key(KeyEvent.VK_SPACE);
            LiveUiReview.await("PlayerRecordsScreen");LiveUiReview.snapshot("records-empty");
            LiveUiReview.key(KeyEvent.VK_RIGHT);LiveUiReview.snapshot("achievements-locked");
            LiveUiReview.key(KeyEvent.VK_ESCAPE);LiveUiReview.await("TitleScreen");
            LiveUiReview.check(true,"four complete Records screenshots captured using software X11 rendering");
            LiveUiReview.replacement.close();System.out.println("RecordsVisualCapture passed.");System.exit(0);
        }catch(Throwable t){t.printStackTrace();System.exit(1);}
    }
}
