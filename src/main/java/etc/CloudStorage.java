/**
 * Copyright (C) 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package etc;

import com.google.appengine.tools.cloudstorage.*;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Objectify;
import ninja.Context;
import ninja.Renderable;
import ninja.Result;
import ninja.Results;
import ninja.i18n.Lang;
import ninja.utils.MimeTypes;
import ninja.utils.ResponseStreams;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.LyricsHandler;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;

import static org.codehaus.plexus.util.FileUtils.removeExtension;


//Singleton
public class CloudStorage {

    private final GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;
    private final Mp3Parser  mp3Parser = new  Mp3Parser();


    /**
     * This is the system wide logger. You can still use any config you like. Or
     * create your own custom logger.
     *
     * But often this is just a simple solution:
     //*/
    @Inject
    public Logger logger;

    @Inject
    Lang lang;

    //private final MimeTypes mimeTypes;

    /*#Inject
    public CloudStorageDao(MimeTypes mimeTypes) {
        this.mimeTypes = mimeTypes;
    }*/

    /**
     *
     * This upload method expects a file and simply displays the file in the
     * multipart upload again to the user (in the correct mime encoding).
     *
     *
     * @return
     * @throws Exception
     * @param context
     */
    public Mp3 uploadMp3(Context context) throws Exception{
        // make sure the context really is a multipart context...
        Mp3 mp3 = null;
        if (context.isMultipart()) {
            // This is the iterator we can use to iterate over the contents of the request.
            try {
            FileItemIterator fileItemIterator = context.getFileItemIterator();

                while (fileItemIterator.hasNext()) {
                    FileItemStream item = fileItemIterator.next();
                    String name = item.getName();

                    GcsFilename filename = new GcsFilename("musaemachine.com", name);

                    InputStream stream = item.openStream();

                    BodyContentHandler handler = new BodyContentHandler();
                    ParseContext pcontext = new ParseContext();
                    Metadata metadata = new Metadata();
                    mp3Parser.parse(stream, handler, metadata, pcontext);

                    Double duration = Double.parseDouble(metadata.get("xmpDM:duration"));
                    mp3 = new Mp3(name,duration);

                    String contentType = item.getContentType();
                    GcsFileOptions options = new GcsFileOptions.Builder()
                            .acl("public-read").mimeType(contentType).build();
                    GcsOutputChannel outputChannel =
                            gcsService.createOrReplace(filename, options);

                    copy(stream, Channels.newOutputStream(outputChannel));

                    GcsFilename filename_ext_rm = new GcsFilename("musaemachine.com", removeExtension(name).concat(".png"));
                    URL url;
                    url = new URL(filename.toString());
                    AudioWaveformCreator awc = new AudioWaveformCreator(url, filename_ext_rm.toString());                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return mp3;
    }


    private void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}

