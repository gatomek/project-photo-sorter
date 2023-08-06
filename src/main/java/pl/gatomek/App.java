package pl.gatomek;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class App {
    private static int c = 0;
    private static String folderPath = "C:/Dokumenty/_Przejrzane zdjęcia";
    private static String targetFolderPath = "C:/Dokumenty/_SortedPhotos";

    public static void main(String[] args) {
        System.out.println("Hello World!");

        File folder = new File(folderPath);
        if (!folder.exists())
            return;

        System.out.println(folder.getAbsolutePath());

        scanFolder(folder);

        System.out.println(Integer.toString(c));
    }

    public static ImageInfo getImageInfo(final File file)
            throws ImageReadException, IOException {
        return Imaging.getImageInfo(file);
    }

    public static String getDateTimeFromFile(File file) {
        String dateTime = null;

        try {
            if (FileUtils.isMovie(file)) {
                dateTime = FileUtils.tryGetDateTimeFromFileName(file);
            } else {
                dateTime = FileUtils.tryGetDateTimeFromMetadata( file);
            }
        } catch (Exception ex) {
        } finally {
            return dateTime;
        }
    }

    public static String makeTargetPath(File file) {
        String dateTime = getDateTimeFromFile(file);

        if (Objects.isNull(dateTime))
            return "/unknown";

        String[] parts = dateTime.split("-");
        String pathPostfix = "/" + parts[0] + "/" + parts[1] + "/" + parts[2];

        return pathPostfix;
    }

    public static File makeUniqueTargetFile(File file) throws IOException {
        String midPath = makeTargetPath(file);
        String folderTargetPath = targetFolderPath + midPath;

        File targetFolderFile = new File(folderTargetPath);
        targetFolderFile.mkdirs();

        String targetPath = folderTargetPath + "/" + file.getName();
        File targetFile = new File(targetPath);

        if (!targetFile.exists())
            return targetFile;

        if (FileUtils.sameContent(file, targetFile)) {
            System.out.println("--- Rejecting file: " + file.getAbsolutePath());
            return null;
        }

        String[] nameParts = file.getName().split("\\.");
        String coreName = nameParts[0];
        String extension = nameParts[1];

        for (int i = 0; i < 100; i++) {
            targetPath = targetFolderPath + midPath + "/" + coreName + "-" + Integer.toString(i) + "." + extension;
            File f = new File(targetPath);

            if (!f.exists())
                return f;

            if (FileUtils.sameContent(file, f)) {
                System.out.println("--- Rejecting file: " + f.getAbsolutePath());
                return null;
            }
        }

        throw new IOException();
    }

    public static void tryCopyFile(File file) {
        try {
            File copied = makeUniqueTargetFile(file);
            if (Objects.nonNull(copied)) {
                System.out.println(file.getAbsolutePath() + " -> " + copied.getAbsolutePath());
                com.google.common.io.Files.copy(file, copied);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void scanFolder(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanFolder(file);
            } else if (file.isFile()) {
                tryCopyFile(file);
            }
        }
    }
}
