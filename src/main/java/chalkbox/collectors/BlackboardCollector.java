package chalkbox.collectors;

import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Collector
public class BlackboardCollector {
    @ConfigItem(key = "gradebook", description = "Location of the blackboard gradebook")
    public String gradebook;

    @DataSet(stream = "submissions")
    public List<Collection> collect(Map<String, String> config) throws IOException {
        ZipFile zip = new ZipFile(gradebook);

        Map<String, Collection> data = new HashMap<>();

        for (ZipEntry entry : zip.stream().collect(Collectors.toList())) {
            if (entry.getName().endsWith(".txt")) {
                Scanner s = new Scanner(zip.getInputStream(entry)).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                Data submissionData = readSubmissionFile(result);
                submissionData.set("root", ".");
                String sid = submissionData.get("Name").toString();

                Collection collection = data.getOrDefault(sid, new Collection(submissionData));
                data.put(sid, collection);

                // TODO: Fix the parsing of filenames to handle multiple files
                // URGENT - i.e 10am tomorrow morning
                InputStream fileStream = zip.getInputStream(
                        zip.getEntry(submissionData.get("\tFilename").toString()));
                File outputFile = new File(collection.getWorking()
                        .getUnmaskedPath(submissionData.get("\tOriginal filename").toString()));
                Files.copy(fileStream, outputFile.toPath());
                collection.getWorking().refresh();
            }
        }

        return new ArrayList<>(data.values());
    }

//        for (Map.Entry<Submission, String> entry : zips.entrySet()) {
//            if (entry.getValue() == null) {
//                continue;
//            }
//
//            BlackboardSubmission submission = (BlackboardSubmission) entry.getKey();
//            ZipFile studentZip;
//            try {
//                studentZip = innerZip(zip, entry.getValue());
//                submission.loadZip(studentZip);
//            } catch (IOException io) {
//                logger.log("Unable to open zip");
//                logger.log(entry.getValue());
//            }
//
//            submission.setSchema(new Schema(submission.getFileNames()));
//        }
//    }

    // Because unzipping a zip in java is equally stupid
    // source: https://stackoverflow.com/questions/11287486/read-a-zip-file-inside-zip-file
    private static ZipFile innerZip(ZipFile zipFile, String zipFileName) throws IOException {
        File tempFile = File.createTempFile("tempFile", "zip");
        tempFile.deleteOnExit();
        InputStream inStream = zipFile.getInputStream(new ZipEntry(zipFileName));
        Files.copy(inStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return new ZipFile(tempFile);
    }

    private static Data readSubmissionFile(String submissionFile) {
        String[] brokenFile = submissionFile.split("\n");
        String lastKey = null;
        Data data = new Data();

        for (String line : brokenFile) {
            if (line.isEmpty()) {
                continue;
            }

            // handle one-line key-pair value
            if (line.contains(": ")) {
                String[] map = line.split(": ", 2);
                data.set(map[0], map[1]);
                // multi-line key
            } else if (line.contains(":")) {
                lastKey = line.split(":")[0];
                // multi-line value
            } else {
                if (data.keys().contains(lastKey)) {
                    data.set(lastKey, data.get(lastKey) + line);
                } else {
                    data.set(lastKey, line);
                }
            }
        }

        // Extract the student number
        Pattern pattern = Pattern.compile("\\((.*)\\)");
        Matcher matcher = pattern.matcher(data.get("Name").toString());
        if (matcher.find()) {
            data.set("sid", matcher.group(1));
        }

        return data;
    }
}
