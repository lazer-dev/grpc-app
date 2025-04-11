package app.service.gradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class DownloadProtoSwaggerGeneratorTask extends DefaultTask {
    private static final String BINARY_PATH = "https://github.com/grpc-ecosystem/grpc-gateway/releases/download/{version}/protoc-gen-openapiv2-{version}-{platform}";

    private String version;
    private String outputPath;

    public void setVersion(String version) {
        this.version = version;
    }

    @Input
    public String getVersion() {
        return version;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    @OutputFile
    public String getOutputPath() {
        return outputPath;
    }

    @TaskAction
    public void run() throws Exception {
        if (isBlank(version)) {
            throw new GradleException("version is required");
        }

        if (isBlank(outputPath)) {
            throw new GradleException("outputPath is required");
        }

        final var file = new File(outputPath);
        if (file.exists()) {
            System.out.println("Skipping download, proto gen binary file exists: " + file.getAbsolutePath());
            return;
        }

        if (!file.createNewFile()) {
            throw new RuntimeException("can not create a new file " + outputPath);
        }

        if (!file.setExecutable(true)) {
            throw new RuntimeException("can not make file an executable " + outputPath);
        }

        final var binaryUrl = downloadUrl();

        System.out.println("Downloading proto gen binary file from " + binaryUrl);
        try (InputStream in = binaryUrl.openStream();
             OutputStream out = new FileOutputStream(file)) {
            in.transferTo(out);
        }

        System.out.println("proto gen binary file downloaded to " + outputPath);
    }

    private URL downloadUrl() throws Exception {
        final var os = OperatingSystem.current();

        final String platform;
        if (os.isMacOsX()) {
            platform = isArm64() ? "darwin-arm64" : "darwin-x86_64";
        } else if (os.isLinux()) {
            platform = isArm64() ? "linux-arm64" : "linux-x86_64";
        } else if (os.isWindows()) {
            platform = isArm64() ? "windows-arm64.exe" : "windows-x86_64.exe";
        } else {
            throw new GradleException("Unsupported OS: " + os);
        }

        final var downloadPath = BINARY_PATH.replace("{version}", version).replace("{platform}", platform);
        return new URI(downloadPath).toURL();
    }

    private boolean isArm64() {
        final var arch = System.getProperty("os.arch").toLowerCase();
        return arch.contains("aarch64") || arch.contains("arm64");
    }
}
