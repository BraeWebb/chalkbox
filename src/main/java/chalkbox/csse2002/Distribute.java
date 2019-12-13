package chalkbox.csse2002;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Finish;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.java.checkstyle.CheckStyle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//todo(issue:#21) Refactor this to not be in CSSE2002, should be generic
/**
 * Process that distributes style marking to tutors in the form of zips.
 *
 * Each tutor in the allocation is provided with a zip named, [tutor-name].zip.
 *
 * Requires that markers have been allocated and checkstyle has been run,
 * satisfied by the processors depends. Should have results of the form:
 * <pre>
 * {
 *     "checkstyle": "[checkstyle-output]",
 *     "marker": "[tutor-name]"
 * }
 * </pre>
 *
 * Each zip contains folders for each allocated student, within the student
 * directory will be populated with the student submission and the following:
 * <ul>
 *     <li>[sid].checkstyle: results of running checkstyle on the src directory</li>
 *     <li>[sid].style: empty .style file copied from style config item</li>
 *     <li>[sid].func: formatted results of running functionality tests,
 *     if functionality was run</li>
 * </ul>
 */
@Processor(depends = {CheckStyle.class, Allocate.class}, threads = 1)
public class Distribute {
    private Map<String, ZipOutputStream> zips = new HashMap<>();

    @ConfigItem(description = "path to the folder where zips will be placed")
    public String zipDirectory;

    @ConfigItem(description = "pipe separated list of tutor names")
    public String tutors;

    @ConfigItem(description = "location of the .style file to copy")
    public String style;
    private String styleContents = null;

    @Prior
    public void makeZips(Map<String, String> config) throws IOException {
        File zipDir = new File(zipDirectory);
        if (!zipDir.exists()) {
            zipDir.mkdirs();
        }

        // read the .style file
        File styleFile = new File(style);
        if (styleFile.exists()) {
            styleContents = new String(Files.readAllBytes(styleFile.toPath()));
        }

        // make a zip for each tutor
        for (String tutor : tutors.split("\\|")) {
            String zipName = zipDir + File.separator + tutor + ".zip";
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(zipName);
            } catch (FileNotFoundException e) {
                System.err.println("Unable to open output stream for zip " + zipName);
                continue;
            }
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            zips.put(tutor, zipOut);
        }
    }

    @Pipe(stream = "submissions")
    public Collection distribute(Collection collection) {
        String markingTutor = collection.getResults().get("marker").toString();
        String sid = collection.getResults().get("sid").toString();
        File source = new File(collection.getSource().getUnmaskedPath());

        ZipOutputStream zipOut = zips.get(markingTutor);

        // copy submission to zip
        try {
            zipFile(source, sid, zipOut);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to add " + sid + " to tutor zip");
        }

        // copy .checkstyle to zip
        try {
            String checkstyle = collection.getResults().get("checkstyle").toString();
            zipString(checkstyle, sid + File.separator + sid + ".checkstyle", zipOut);
        } catch (IOException e) {
            System.err.println("Unable to add .checkstyle to " + sid);
        }

        // copy .style to zip
        try {
            zipString(styleContents, sid + File.separator + sid + ".style", zipOut);
        } catch (IOException e) {
            System.err.println("Unable to add .style to " + sid);
        }

        return collection;
    }

    /**
     * Close all the zip output streams after use.
     */
    @Finish
    public void close() {
        for (ZipOutputStream outs : zips.values()) {
            try {
                outs.close();
            } catch (IOException e) {
                System.err.println("IO Error closing zip ");
            }
        }
    }

    private static void zipString(String string, String name, ZipOutputStream zipOut) throws IOException {
        InputStream stream = new ByteArrayInputStream(string.getBytes());
        zipStream(stream, name, zipOut);
    }

    private static void zipStream(InputStream stream, String name, ZipOutputStream zipOut) throws IOException {
        ZipEntry zipEntry = new ZipEntry(name);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = stream.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.closeEntry();
    }

    // shamelessly stolen from https://www.baeldung.com/java-compress-and-uncompress
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        zipStream(fis, fileName, zipOut);
        fis.close();
    }
}
