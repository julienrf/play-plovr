package play.modules.plovr;

import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Result;
import org.apache.commons.io.FileUtils;
import org.plovr.*;
import play.Logger;
import play.Play;
import play.vfs.VirtualFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;

public class Plovr {
    
    private static File[] depFiles = new File[0];
    public static String serverAddress = "localhost";
    public static int serverPort = 9810;
    public static String buildDirectory = "public" + File.separator + "plovr";

    public static void load() {
        serverAddress = Play.configuration.getProperty("plovr.server.address", serverAddress);
        serverPort = Integer.parseInt(Play.configuration.getProperty("plovr.server.port", "" + serverPort));
        buildDirectory = Play.configuration.getProperty("plovr.build.target", buildDirectory);

        depFiles = VirtualFile.open(new File(Play.applicationPath, "conf" + File.separator + "plovr")).getRealFile().listFiles();

        if (Play.mode == Play.Mode.DEV) { // In DEV mode, start Plovr deamon
            start();
        } else { // In PROD mode, build static files
            build();
        }
    }

    private static void start() {
        CompilationServer server = new CompilationServer(serverAddress, serverPort);
        for (File depFile : depFiles) {
            try {
                Config config = ConfigParser.parseFile(depFile);
                server.registerConfig(config);
            } catch (IOException e) {
                Logger.error("Unable to parse file '%s', error is: %s",depFile.getName(), e.getMessage());
            }
        }
        Logger.info("Starting plovr server on http://%s:%s", serverAddress, serverPort);
        server.run();
    }

    private static void build() {
        Logger.info("Compiling Javascript files ...");
        File target = new File(Play.applicationPath, buildDirectory);
        FileUtils.deleteQuietly(target);
        target.mkdirs();
        for (File depFile : depFiles) {
            try {
                Config config = ConfigParser.parseFile(depFile);
                Compilation compilation = CompileRequestHandler.compile(config);
                File compiled = new File(target, config.getId() + ".js"); // Convention: the name of the id is used
                Result result = compilation.getResult();
                for (JSError warning : result.warnings) {
                    Logger.warn("'%s' :: %s", depFile.getName(), warning);
                }
                Logger.debug("Compiled %s into %s bytes", depFile.getName(), compilation.getCompiledCode().length());
                Writer writer = new FileWriter(compiled);
                Logger.debug("fooo");
                writer.write(compilation.getCompiledCode());
                if (result.success) {
                    Logger.debug("'%s' successfully compiled into '%s'", depFile.getName(), compiled.getAbsolutePath());
                } else {
                    Logger.error("Could not build '%s', errors are:", depFile.getName());
                    for (JSError error : result.errors) {
                        Logger.error(error.toString());
                    }
                }
            } catch (Exception e) {
                Logger.error("Unable to build file '%s', error is: %s", depFile.getName(), e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
