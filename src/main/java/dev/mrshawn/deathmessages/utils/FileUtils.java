/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.mrshawn.deathmessages.utils;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * org.apache.commons.io.FileUtils
 */
public class FileUtils {

    public static void copyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
            throws IOException {
        copyFile(srcFile, destFile,
                preserveFileDate
                        ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING}
                        : new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
    }

    public static void copyFile(final File srcFile, final File destFile, final CopyOption... copyOptions)
            throws IOException {
        requireFileCopy(srcFile, destFile);
        requireFile(srcFile, "srcFile");
        requireCanonicalPathsNotEquals(srcFile, destFile);
        mkdirs(getParentFile(destFile));

        Objects.requireNonNull(destFile, "destFile");
        if (destFile.exists()) {
            requireFile(destFile, "destFile");
            Objects.requireNonNull(destFile, "file");
            if (!destFile.canWrite()) {
                throw new IllegalArgumentException("File parameter '" + destFile + " is not writable: 'destFile'");
            }
        }

        Files.copy(srcFile.toPath(), destFile.toPath(), copyOptions);
        requireEqualSizes(srcFile, destFile, srcFile.length(), destFile.length());
    }

    public static void copyFileToDirectory(final File srcFile, final File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    public static void copyFileToDirectory(final File sourceFile, final File destinationDir, final boolean preserveFileDate)
            throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        Objects.requireNonNull(destinationDir, "destinationDir");
        if (destinationDir.exists()) {
            Objects.requireNonNull(destinationDir, "destinationDir");
            if (!destinationDir.isDirectory()) {
                throw new IllegalArgumentException("Parameter 'destinationDir' is not a directory: '" + destinationDir + "'");
            }
        }
        copyFile(sourceFile, new File(destinationDir, sourceFile.getName()), preserveFileDate);
    }

    private static File getParentFile(final File file) {
        return file == null ? null : file.getParentFile();
    }

    private static void mkdirs(final File directory) throws IOException {
        if ((directory != null) && (!directory.mkdirs() && !directory.isDirectory())) {
            throw new IOException("Cannot create directory '" + directory + "'.");
        }
    }

    private static void requireCanonicalPathsNotEquals(final File file1, final File file2) throws IOException {
        final String canonicalPath = file1.getCanonicalPath();
        if (canonicalPath.equals(file2.getCanonicalPath())) {
            throw new IllegalArgumentException(String
                    .format("File canonical paths are equal: '%s' (file1='%s', file2='%s')", canonicalPath, file1, file2));
        }
    }

    private static void requireEqualSizes(final File srcFile, final File destFile, final long srcLen, final long dstLen)
            throws IOException {
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile
                    + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
    }

    private static void requireFile(final File file, final String name) {
        Objects.requireNonNull(file, name);
        if (!file.isFile()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
        }
    }

    private static void requireFileCopy(final File source, final File destination) throws FileNotFoundException {
        Objects.requireNonNull(source, "source");
        if (!source.exists()) {
            throw new FileNotFoundException(
                    "File system element for parameter 'source' does not exist: '" + source + "'");
        }
        Objects.requireNonNull(destination, "destination");
    }
}
