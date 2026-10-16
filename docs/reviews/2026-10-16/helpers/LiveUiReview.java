import engine.*;
import records.*;
import screen.*;
import audio.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Independent reviewer: real AWT window and OS keyboard input on an isolated X server. */
public final class LiveUiReview {
    static Robot robot;
    static Path output;
    static int checks;
    static RecordsService replacement;
    static Field field(Class<?> type,String name) throws Exception {
        while(type!=null) { try { Field f=type.getDeclaredField(name);f.setAccessible(true);return f; }
            catch(NoSuchFieldException e) {type=type.getSuperclass();} }
        throw new NoSuchFieldException(name);
    }
    static Object get(Object o,String n) throws Exception {return field(o.getClass(),n).get(o);}
    static void set(Object o,String n,Object v) throws Exception {field(o.getClass(),n).set(o,v);}
    static Object current() throws Exception {return field(Core.class,"currentScreen").get(null);}
    static void check(boolean ok,String text) {if(!ok) throw new AssertionError(text);checks++;System.out.println("PASS: "+text);}
    static Object await(String name) throws Exception {
        long end=System.currentTimeMillis()+10000;
        do {Object o=current();if(o!=null && o.getClass().getSimpleName().equals(name)) {
            Thread.sleep(1200);return o;}Thread.sleep(30);
        } while(System.currentTimeMillis()<end);
        throw new AssertionError("Timed out waiting for "+name+"; got "+current());
    }
    static void key(int key) throws Exception {
        robot.waitForIdle();String before=inputState();long start=System.nanoTime();
        robot.keyPress(key);boolean changed=false;
        try {
            do {Thread.sleep(5);changed=!before.equals(inputState());}
            while(!changed && (System.nanoTime()-start)<1500000000L);
        } finally {robot.keyRelease(key);}
        System.out.println("Robot key "+KeyEvent.getKeyText(key)+" held "+((System.nanoTime()-start)/1000000)+" ms; stateChanged="+changed);
        if(!changed)throw new AssertionError("Keyboard action was not handled: "+KeyEvent.getKeyText(key)+"; state="+before);
        robot.waitForIdle();Thread.sleep(400);
    }
    static String inputState() throws Exception {
        Object o=current();StringBuilder b=new StringBuilder(o.getClass().getSimpleName()+"@"+System.identityHashCode(o));
        for(String n:new String[]{"returnCode","tab","selectedOption","waitingForKey","waitingForKeyRelease","nameCharSelected","bulletsShot","isRunning"}) {
            try {b.append('|').append(n).append('=').append(get(o,n));}catch(NoSuchFieldException ignored){}
        }
        try {b.append("|shipX=").append(((entity.Ship)get(o,"ship")).getPositionX());}catch(NoSuchFieldException ignored){}
        try {b.append("|name=").append(new String((char[])get(o,"name")));}catch(NoSuchFieldException ignored){}
        b.append("|bgm=").append(AudioManager.getBGMVolume()).append("|left=").append(ControlsConfig.getKey(ControlsConfig.LEFT));
        return b.toString();
    }
    static void snapshot(String name) throws Exception {
        engine.Frame frame=(engine.Frame)field(Core.class,"frame").get(null);
        Point p=frame.getLocationOnScreen();Dimension d=frame.getSize();
        ImageIO.write(robot.createScreenCapture(new Rectangle(p,d)),"png",output.resolve(name+".png").toFile());
    }
    static void freshProfile(String name) throws Exception {
        RecordsService previous=Core.getRecordsService();
        replacement=new RecordsService(output.resolve(name+".properties"));
        field(Core.class,"recordsService").set(null,replacement);
        previous.close();
    }
    static void recordsUi() throws Exception {
        await("TitleScreen");
        key(KeyEvent.VK_DOWN);key(KeyEvent.VK_DOWN);
        check(((Integer)get(current(),"returnCode"))==9,"menu selects Records route 9; actual="+get(current(),"returnCode"));
        snapshot("menu-records-selected");key(KeyEvent.VK_SPACE);
        Object page=await("PlayerRecordsScreen");check((Integer)get(page,"tab")==0,"Records opens on Run Records");
        snapshot("records-populated");key(KeyEvent.VK_RIGHT);
        check((Integer)get(page,"tab")==1,"Right opens Achievements tab");snapshot("achievements-unlocked");
        key(KeyEvent.VK_LEFT);check((Integer)get(page,"tab")==0,"Left returns to Run Records");
        key(KeyEvent.VK_ESCAPE);await("TitleScreen");check(true,"Escape returns from Records to menu");
        key(KeyEvent.VK_DOWN);key(KeyEvent.VK_DOWN);key(KeyEvent.VK_SPACE);await("PlayerRecordsScreen");
        key(KeyEvent.VK_SPACE);await("TitleScreen");check(true,"Space returns from Records to menu");
        key(KeyEvent.VK_DOWN);key(KeyEvent.VK_SPACE);await("HighScoreScreen");snapshot("legacy-high-scores");
        key(KeyEvent.VK_SPACE);await("TitleScreen");check(true,"legacy High scores opens and returns");
        for(int i=0;i<3;i++)key(KeyEvent.VK_DOWN);key(KeyEvent.VK_SPACE);await("SettingsScreen");
        snapshot("settings-menu");key(KeyEvent.VK_SPACE);await("AudioControlScreen");
        int volume=AudioManager.getBGMVolume();key(KeyEvent.VK_RIGHT);
        check(AudioManager.getBGMVolume()==volume+5,"audio settings Right increases BGM volume");
        key(KeyEvent.VK_LEFT);check(AudioManager.getBGMVolume()==volume,"audio settings Left restores BGM volume");
        key(KeyEvent.VK_ESCAPE);await("SettingsScreen");key(KeyEvent.VK_DOWN);key(KeyEvent.VK_SPACE);
        await("ControlsSettingsScreen");snapshot("controls-settings");
        key(KeyEvent.VK_SPACE);key(KeyEvent.VK_J);
        check(ControlsConfig.getKey(ControlsConfig.LEFT)==KeyEvent.VK_J,"controls setting accepts a new left-movement key");
        key(KeyEvent.VK_SPACE);key(KeyEvent.VK_LEFT);
        check(ControlsConfig.getKey(ControlsConfig.LEFT)==KeyEvent.VK_LEFT,"controls setting restores original left key");
        key(KeyEvent.VK_ESCAPE);await("SettingsScreen");key(KeyEvent.VK_DOWN);key(KeyEvent.VK_DOWN);key(KeyEvent.VK_SPACE);
        await("GameplaySettingsScreen");snapshot("gameplay-settings");key(KeyEvent.VK_ESCAPE);await("SettingsScreen");
        check(true,"all three Settings subpages open and return");key(KeyEvent.VK_ESCAPE);await("TitleScreen");
        freshProfile("empty-profile");key(KeyEvent.VK_DOWN);key(KeyEvent.VK_DOWN);key(KeyEvent.VK_SPACE);
        await("PlayerRecordsScreen");snapshot("records-empty");key(KeyEvent.VK_RIGHT);snapshot("achievements-locked");
        key(KeyEvent.VK_ESCAPE);await("TitleScreen");check(true,"empty Records and locked Achievements render and return");
    }
    static void notifications(boolean infinite) throws Exception {
        freshProfile(infinite?"infinite-fixture":"campaign-fixture");
        await("TitleScreen");key(infinite?KeyEvent.VK_I:KeyEvent.VK_SPACE);
        Object game=await(infinite?"InfiniteGameScreen":"GameScreen");
        long readyDeadline=System.currentTimeMillis()+10000;
        while(!((Cooldown)get(game,"inputDelay")).checkFinished() && System.currentTimeMillis()<readyDeadline)Thread.sleep(20);
        check(((Cooldown)get(game,"inputDelay")).checkFinished(),"normal start countdown completes before movement test ("+infinite+")");
        check((Boolean)get(game,"isRunning"),"game running in "+game.getClass().getSimpleName());
        RunSession session=(RunSession)get(game,"runSession");String id=session.getRunId();
        for(int i=0;i<20;i++)replacement.enemyDefeated(id,"review-fixture-enemy-"+i);
        replacement.activeDuration(id,60000); // Synthetic event fixture; not a natural 60-second playthrough.
        long firstShown=System.currentTimeMillis();
        while(get(game,"visibleAchievement")==null && System.currentTimeMillis()-firstShown<1500)Thread.sleep(10);
        firstShown=System.currentTimeMillis();
        check(get(game,"visibleAchievement")==Achievement.FIRST_ENEMY,"FIFO notice starts with First Contact ("+infinite+")");
        snapshot((infinite?"infinite":"campaign")+"-notice-first");
        entity.Ship ship=(entity.Ship)get(game,"ship");int x=ship.getPositionX();
        key(KeyEvent.VK_RIGHT);check(ship.getPositionX()>x,"movement continues while achievement notice is visible ("+infinite+")");
        key(KeyEvent.VK_SPACE);check((Integer)get(game,"bulletsShot")>0,"shooting continues while achievement notice is visible ("+infinite+")");
        while(get(game,"visibleAchievement")==Achievement.FIRST_ENEMY && System.currentTimeMillis()-firstShown<4500)Thread.sleep(10);
        long elapsed=System.currentTimeMillis()-firstShown;
        check(get(game,"visibleAchievement")==Achievement.TWENTY_ENEMIES,"second queued notice is Ace Pilot ("+infinite+")");
        check(elapsed>=2700 && elapsed<3500,"first notice lasts about three seconds: "+elapsed+" ms ("+infinite+")");
        snapshot((infinite?"infinite":"campaign")+"-notice-second");
        long second=System.currentTimeMillis();
        while(get(game,"visibleAchievement")==Achievement.TWENTY_ENEMIES && System.currentTimeMillis()-second<4500)Thread.sleep(10);
        check(get(game,"visibleAchievement")==Achievement.SIXTY_SECONDS,"third queued notice is Still Flying ("+infinite+")");
        snapshot((infinite?"infinite":"campaign")+"-notice-third");
        // Deterministic result fixture: exhaust lives, retaining real engine end-screen routing.
        set(game,"lives",0);Object result=await("ScoreScreen");
        java.util.List<?> unlocks=(java.util.List<?>)get(result,"runUnlocks");
        check(unlocks.size()==4,"summary retains three gameplay unlocks plus First Flight ("+infinite+")");
        snapshot((infinite?"infinite":"campaign")+"-summary");
        check(replacement.getRecordCount()==1,"one completed run reaches Records ("+infinite+")");
        if((Boolean)get(result,"isNewRecord")) {
            int selected=(Integer)get(result,"nameCharSelected");key(KeyEvent.VK_RIGHT);
            check((Integer)get(result,"nameCharSelected")==((selected+1)%3),"high-score name cursor still moves ("+infinite+")");
            int pos=(Integer)get(result,"nameCharSelected");char before=((char[])get(result,"name"))[pos];key(KeyEvent.VK_UP);
            check(((char[])get(result,"name"))[pos]!=before,"high-score name character still changes ("+infinite+")");
        }
        key(KeyEvent.VK_SPACE);Object replay=await(infinite?"InfiniteGameScreen":"GameScreen");
        check(replay!=game,"Space replays the same mode ("+infinite+")");
        if(infinite)key(KeyEvent.VK_ESCAPE);else set(replay,"lives",0);
        await("ScoreScreen");key(KeyEvent.VK_ESCAPE);await("TitleScreen");
        check(true,"result Escape returns to title ("+infinite+")");
    }
    static void audioResources() throws Exception {
        int files=0;
        try(java.util.stream.Stream<Path> paths=Files.walk(Paths.get(System.getProperty("review.res"),"audio"))) {
            for(Path p:(Iterable<Path>)paths.filter(p->p.toString().endsWith(".wav"))::iterator) {
                try(javax.sound.sampled.AudioInputStream in=javax.sound.sampled.AudioSystem.getAudioInputStream(p.toFile())) {
                    check(in.getFrameLength()>0,"WAV decodes: "+p.getFileName());files++;
                }
            }
        }
        System.out.println("Decoded WAV assets: "+files+"; playback/listening remains unverified without an audio device.");
    }
    public static void main(String[] args) throws Exception {
        output=Paths.get(args[0]);Files.createDirectories(output);robot=new Robot();
        String mode=args.length>1?args[1]:"notifications";
        if(mode.startsWith("records")) {
            Path profile=Paths.get(System.getProperty("user.home"),".space-invaders","records-achievements.properties");
            try(Seed seed=new Seed(profile)) {seed.populate();}
        }
        Thread game=new Thread(()->Core.main(new String[0]),"review-game-engine");game.start();
        try {
            await("TitleScreen");
            engine.Frame frame=(engine.Frame)field(Core.class,"frame").get(null);
            javax.swing.SwingUtilities.invokeAndWait(()->{frame.toFront();frame.requestFocus();});
            if(mode.startsWith("records"))recordsUi();
            if(!mode.equals("records-only")){notifications(true);notifications(false);}
            audioResources();
            System.out.println("LiveUiReview passed: "+checks+" checks. Synthetic event/lives fixtures; actual Robot keys and screenshots.");
            if(replacement!=null)replacement.close();System.exit(0);
        } catch(Throwable t) {t.printStackTrace();try{snapshot("failure");}catch(Throwable ignored){}if(replacement!=null)replacement.close();System.exit(1);}
    }
    static class Seed implements AutoCloseable {
        RecordsService service;
        Seed(Path p){service=new RecordsService(p);}
        void populate(){for(int i=0;i<10;i++) {service.startRun("fixture-"+i);service.completeRun(new RunRecord("fixture-"+i,1000+i*100,20+i,60000+i*1000,1792110000000L+i));}}
        public void close(){service.close();}
    }
}
