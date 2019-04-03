package chalkbox.api.files;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * An abstraction of a source file
 */
public abstract class SourceFile extends SimpleJavaFileObject {
    /**
     * Construct a source file from a URI
     *
     * @param uri The unique resource identifier of the file (analogous to path)
     */
    public SourceFile(String uri) {
        super(buildUri(uri), Kind.SOURCE);
    }

    /**
     * Hash the contents of the file to provide a unique hash of the file
     *
     * @return The hash of the file contents
     * @throws IOException If an error occurs reading the file contents
     */
    public String toHash() throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException algo) {
            return null; // will surely never occur
        }

        byte[] hash = digest.digest(getCharContent(true)
                .toString().getBytes());

        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Build a URI from a base URI
     *
     * @param uri A base url
     * @return A URI
     */
    protected static URI buildUri(String uri) {
        try {
            uri = URLEncoder.encode(uri, "utf-8");
        } catch (UnsupportedEncodingException e) {

        }

        return URI.create("source:///" + uri);
    }

    /**
     * Get the string contents of the source file
     *
     * @return String contents of the source file
     * @throws IOException If an error occurs reading the file contents
     */
    public String getContent() throws IOException {
        return getCharContent(true).toString();
    }
}
