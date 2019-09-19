/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.egacryptor.stream.pipeline;

import org.bouncycastle.util.Arrays;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultStream implements PipelineStream {

    private final InputStream sourceInputStream;
    private final OutputStream targetOutputStream;
    private final int bufferSize;

    public DefaultStream(final InputStream sourceInputStream, final OutputStream targetOutputStream,
                         final int bufferSize) {
        this.sourceInputStream = sourceInputStream;
        this.targetOutputStream = targetOutputStream;
        this.bufferSize = bufferSize;
    }

    @Override
    public long execute() throws IOException {
        final byte[] buffer = new byte[bufferSize];
        try {
            long totalRead = 0;
            int bytesRead;
            while ((bytesRead = sourceInputStream.read(buffer)) > 0) {
                totalRead += bytesRead;
                targetOutputStream.write(buffer, 0, bytesRead);
            }
            targetOutputStream.flush();
            return totalRead;
        } finally {
            Arrays.fill(buffer, (byte) 0);
        }
    }

    @Override
    public void close() throws IOException {
        sourceInputStream.close();
        targetOutputStream.close();
    }
}
