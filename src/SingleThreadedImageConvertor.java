import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
public class SingleThreadedImageConvertor {

    private static final int MAX_IMAGES = 500;
    private static final String FILE_FORMAT = "jpg";
    private static final String FILE_PATH = "/Users/noobie/Desktop/549/";

    public static void main(String[] args) {
        try {
            final File directory = new File(FILE_PATH+"ColorImages");
            final File[] files = directory.listFiles();
            final List<String> images =
                    Stream.of(files)
                            .filter(file -> file != null)
                            .map(file -> file.getName())
                            .filter(file -> file.endsWith(FILE_FORMAT))
                            .limit(MAX_IMAGES)
                            .collect(Collectors.toList());
            final long startTime = System.currentTimeMillis();
            images.forEach(image -> renderImage(image));

            System.out.println("SingleThreaded Version...");
            System.out.println("Total time taken in milliseconds to Render 500 Images : "+(System.currentTimeMillis() - startTime));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void renderImage(final String image) {
        try {
            File file = new File(FILE_PATH + "ColorImages/" + image);
            BufferedImage img = ImageIO.read(file);
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    int pixel = img.getRGB(j, i);
                    Color color = new Color(pixel, true);
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    final int average = (red + green + blue) / 3;
                    color = new Color(average, average, average);
                    img.setRGB(j, i, color.getRGB());
                }
            }
            file = new File(FILE_PATH+"bwImages/bw_" + image);
            ImageIO.write(img, FILE_FORMAT, file);
        } catch (IOException e) {

        }

    }


}
