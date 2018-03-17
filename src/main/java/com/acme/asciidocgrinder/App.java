package com.acme.asciidocgrinder;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.asciidoctor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
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

    private static final int CANT_ADD_HIGHLIGHTER = -4;

    private static final int HIGHLIGHTER_EXTRACTION_FAILED = -5;

    private static final int TECHNICAL_EXCEPTION = -6;

    private final static String HIGHLIGHTJS_DIR_NAME = "highlight";

    private final static String DEFAULT_OUTPUT_DIR = "dist";

    private final static Attributes HTML_SETUP = attributes()
            .sourceHighlighter("highlightjs")
            .attribute("highlightjsdir", HIGHLIGHTJS_DIR_NAME)
            .attribute("highlightjs-theme", "monokai")
            .get();

    private final static Attributes PDF_SETUP = attributes()
            .sourceHighlighter("rouge")
            .attribute("rouge-style", "monokai")
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

        // Push custom hilightjs version if not exists
        final String zipExtractDir = workingDirectory.getAbsolutePath() + File.separator + HIGHLIGHTJS_DIR_NAME;
        final File hilightDir = new File(zipExtractDir);
        if (hilightDir.exists() && hilightDir.isFile()) {
            logger.error("Can't create {} folder : file already exists !", HIGHLIGHTJS_DIR_NAME);
            System.exit(CANT_ADD_HIGHLIGHTER);
        }

        if (!hilightDir.isDirectory() && !hilightDir.exists()) {
            final URL zipPath = App.class.getResource('/' + HIGHLIGHTJS_DIR_NAME + ".zip");
            final File zip;
            try {
                zip = Paths.get(zipPath.toURI()).toFile();
                ZipFile zipFile = new ZipFile(zip);
                zipFile.extractAll(zipExtractDir);
            } catch (URISyntaxException e) {
                logger.error("Can't found archive in classpath {}", e.getMessage());
                System.exit(TECHNICAL_EXCEPTION);
            } catch (ZipException e) {
                logger.error("Can't extract highlighter files : {}", e.getMessage());
                System.exit(HIGHLIGHTER_EXTRACTION_FAILED);
            }
        }

        parseAsciidoc(workingDirectory, HTML5_BACKEND, HTML_SETUP);

        parseAsciidoc(workingDirectory, PDF_BACKEND, PDF_SETUP);
    }

    private static void parseAsciidoc(final File inputDir, final String backendName, final Attributes config) {
        final Asciidoctor asciidoctor = create();
        final File destinationDir = new File(inputDir.getParent() + File.separator + DEFAULT_OUTPUT_DIR);
        final Map<String, Object> options = options()
                .safe(SafeMode.UNSAFE)
                .baseDir(inputDir)
                .destinationDir(destinationDir)
                .mkDirs(true)
                .inPlace(false)
                .attributes(config)
                .backend(backendName)
                .asMap();
        final DirectoryWalker directory = new AsciiDocDirectoryWalker(inputDir.getAbsolutePath());
        asciidoctor.convertDirectory(directory, options);
    }
}
