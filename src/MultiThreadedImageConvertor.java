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
public class MultiThreadedImageConvertor {

    private static final int MAX_IMAGES = 500;
    private static final int BLOCK_SIZE = 80;
    private static final int THREAD_COUNT = 20;
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
            final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            final List<CompletableFuture> futureList = new ArrayList<>();
            final long startTime = System.currentTimeMillis();
            images.forEach(image -> {
                CompletableFuture<Void> future =
                        CompletableFuture.runAsync(() -> {
                            renderImage(image);
                            },executorService);
                futureList.add(future);
            });

            CompletableFuture<Void> allTask = CompletableFuture.allOf(futureList.toArray(new CompletableFuture<?>[futureList.size()]));
            executorService.shutdown();
            while (!executorService.isTerminated()){}
            System.out.println("MultiThreaded Version...");
            System.out.println("Resource Configuration : 8-Core CPU, Fixed Thread Pool of size 20");
            System.out.println("Total time taken in milliseconds to Render 500 Images : "+(System.currentTimeMillis() - startTime));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void renderImage(final String image) {
        try {
            File file = new File(FILE_PATH + "ColorImages/" + image);
            BufferedImage img = ImageIO.read(file);

            final int rowBlockSize = img.getHeight() / BLOCK_SIZE;
            final List<Integer> blocks = new ArrayList<>();
            for (int i = 0; i < rowBlockSize + 1; i++) {
                blocks.add(i * BLOCK_SIZE);
            }
            for (int i = 0; i < blocks.size() - 1; i++) {
                final Integer start = blocks.get(i);
                final Integer end = blocks.get(i + 1);
                CompletableFuture<Void> imageFuture =
                        CompletableFuture.runAsync(
                                () -> {
                                    int width = img.getWidth();
                                    for (int x = start; x < Integer.min(end, img.getHeight()); x++) {
                                        for (int y = 0; y < width; y++) {
                                            int pixel = img.getRGB(y, x);
                                            Color color = new Color(pixel, true);
                                            int red = color.getRed();
                                            int green = color.getGreen();
                                            int blue = color.getBlue();
                                            int average = (red + green + blue) / 3;
                                            color = new Color(average, average, average);
                                            img.setRGB(y, x, color.getRGB());
                                        }
                                    }
                                });
                imageFuture.get();
            }

            file = new File(FILE_PATH+"bwImages/bw_" + image);
            ImageIO.write(img, FILE_FORMAT, file);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

}
