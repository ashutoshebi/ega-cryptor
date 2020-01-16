/*
 *
 * Copyright 2020 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ega.egacryptor.runner;

import joptsimple.OptionParser;

public class CommandLineOptionParser {

    //Input file path
    public static final String FILE_TO_ENCRYPT_PATH = "i";
    //Output folder path
    public static final String OUTPUT_FOLDER_PATH = "o";
    //No of cores minus 1
    public static final String UTILIZE_FULL_RESOURCE = "f";
    //75% of total no of cores
    public static final String UTILIZE_OPTIMIZE_RESOURCE = "m";
    //50% of total no of cores
    public static final String UTILIZE_HALF_RESOURCE = "l";
    //User specified no of threads
    public static final String USER_THREADS = "t";
    //Help option
    public static final String OPTIONS_HELP = "h";
    private static final OptionParser optionParser = buildParser();

    private static OptionParser buildParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(FILE_TO_ENCRYPT_PATH, "File(s) to encrypt. Provide file/folder path or comma separated file path if multiple files in double quotes").
                withRequiredArg().
                required().
                ofType(String.class);
        parser.accepts(OUTPUT_FOLDER_PATH, "Path of the output file. This is optional. If not provided then output files will be generated in the same path as that of source file").
                withRequiredArg().
                ofType(String.class).
                defaultsTo("output-files");
        parser.accepts(UTILIZE_FULL_RESOURCE, "Set this option to allow application to create maximum threads to utilize full capacity of cores/processors available on machine");
        parser.accepts(UTILIZE_OPTIMIZE_RESOURCE, "Set this option to allow application to create maximum threads equals to 75% capacity of cores/processors available on machine");
        parser.accepts(UTILIZE_HALF_RESOURCE, "Set this option to allow application to create maximum threads equals to 50% capacity of cores/processors available on machine");
        parser.accepts(USER_THREADS, "Set this option if user wants to control application to create maximum threads as specified. " +
                "Application will calculate no. of cores/processors available on machine & will create threads accordingly").
                withRequiredArg().
                ofType(Integer.class);
        parser.accepts(OPTIONS_HELP, "Use this option to get help");
        parser.allowsUnrecognizedOptions();
        return parser;
    }

    public static OptionParser getOptionParser() {
        return optionParser;
    }
}
