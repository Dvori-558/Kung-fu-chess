import javax.imageio.ImageIO;
import java.io.File;

public class CheckPng {
    public static void main(String[] args) throws Exception {
        String p = ".github/CTD26/pieces2/RW/states/idle/sprites/1.png";
        File f = new File(p);
        System.out.println("exists=" + f.exists() + " path=" + f.getAbsolutePath());
        var img = ImageIO.read(f);
        System.out.println(img == null ? "img=null" : ("img=" + img.getWidth() + "x" + img.getHeight()));
    }
}
