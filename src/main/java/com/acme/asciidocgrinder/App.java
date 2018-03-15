package com.acme.asciidocgrinder;

import org.asciidoctor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;

/**
 * Simple wrapper for AsciidoctorJ
 */
class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    private final static String HELP_MSG = "Usage : "
            + App.class.getName() + " [asciiDocRootDir]";

    private final static int WRONG_ARGS = -2;

    private final static int NOT_A_DIRECTORY = -3;

    private final static String DEFAULT_OUTPUT_DIR = "dist";

    private final static Attributes DEFAULT_SETUP = attributes()
            .sourceHighlighter("highlightjs")
            .attribute("highlightjsdir", "highlight")
            .get();

    private final static String HTML5_BACKEND = "html5";
    private final static String PDF_BACKEND = "pdf";

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error(HELP_MSG);
            System.exit(WRONG_ARGS);
        }
        final File workingDirectory = new File(args[0]);
        if (!workingDirectory.isDirectory()) {
            logger.error(HELP_MSG);
            System.exit(NOT_A_DIRECTORY);
        }

        parseAsciidoc(workingDirectory, HTML5_BACKEND);

        parseAsciidoc(workingDirectory, PDF_BACKEND);
    }

    private static void parseAsciidoc(final File inputDir, final String backendName) {
        final Asciidoctor asciidoctor = create();
        final File destinationDir = new File(inputDir.getParent() + File.separator + DEFAULT_OUTPUT_DIR);
        final Map<String, Object> options = options()
                .safe(SafeMode.UNSAFE)
                .baseDir(inputDir)
                .destinationDir(destinationDir)
                .mkDirs(true)
                .inPlace(false)
                .attributes(DEFAULT_SETUP)
                .backend(backendName)
                .asMap();
        final DirectoryWalker directory = new AsciiDocDirectoryWalker(inputDir.getAbsolutePath());
        asciidoctor.convertDirectory(directory, options);
    }
}
