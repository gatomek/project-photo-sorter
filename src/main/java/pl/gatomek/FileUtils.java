package pl.gatomek;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static org.apache.commons.imaging.Imaging.getMetadata;

public class FileUtils {
    private FileUtils() {
    }

    private static long calcCrc32(File file) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        Checksum checksum = new CRC32();
        checksum.update(data);
        return checksum.getValue();
    }

    public static boolean sameContent(File sourceFile, File targetFile) throws IOException {
        return calcCrc32(sourceFile) == calcCrc32(targetFile);
    }

    private static boolean checkYear( String year) {
        return Integer.parseInt( year) > 2000;
    }

    public static String tryGetDateTimeFromFileName(File file) {
        String name = file.getName();

        if( ! name.contains( "_"))
            return null;

        String[] splits = name.split( "_");
        String date = splits[0];

        if( date.length() == 8) {
            String year = date.substring(0,4);
            if( ! checkYear(year))
                return null;

            String month = date.substring( 4,6);
            int m = Integer.parseInt( month);
            if( m < 1 || m > 12)
                return null;

            String day = date.substring(6, 8);
            int d = Integer.parseInt( day);
            if( d < 1 || d > 31)
                return null;

            return year + "-" + month + "-" + day;
        }

        return null;
    }

    public static ImageMetadata getMetadata(final File file)
            throws ImageReadException, IOException {
        return Imaging.getMetadata(file);
    }

    public static String tryGetDateTimeFromMetadata( File file) throws IOException, ImageReadException {
        String dateTime = null;

        ImageMetadata metadata = getMetadata(file);
        List<ImageMetadata.ImageMetadataItem> mitems = (List<ImageMetadata.ImageMetadataItem>) metadata.getItems();
        for (ImageMetadata.ImageMetadataItem item : mitems) {
            String value = item.toString();
            if (value.startsWith("DateTime")) {

                int start = value.indexOf('\'');
                int end = value.lastIndexOf('\'');
                String text = value.substring(start + 1, end);

                String date = text.split(" ")[0];

                String[] parts = date.split(":");
                dateTime = parts[0] + "-" + parts[1] + "-" + parts[2];
                break;
            }
        }

        return dateTime;
    }

    public static boolean isMovie(File file) {
        String fileName = file.getName();
        if (fileName.toLowerCase().endsWith(".mp4"))
            return true;

        if (fileName.toLowerCase().endsWith(".mov"))
            return true;

        if (fileName.toLowerCase().endsWith(".avi"))
            return true;

        return false;
    }
}
