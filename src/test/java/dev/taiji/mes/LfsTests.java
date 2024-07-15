package dev.taiji.mes;

import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lfs.BuiltinLFS;
import org.eclipse.jgit.lfs.InstallBuiltinLfsCommand;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.LfsFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

import static dev.taiji.mes.JgitStarterApplicationTests.pathOfUri;

@SpringBootTest
public class LfsTests {

    static Dotenv dotenv;


    @BeforeAll
    static void contextLoads() {
        if (!new File("repo").exists()) {
            new File("repo").mkdir();
        }
        dotenv = Dotenv
                .configure()
                .systemProperties()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        Assert.notNull(dotenv, "dotenv is required, please create .env file in the root directory of the project");
    }


    @Test
    void testAvailable() throws Exception {
        String uri = dotenv.get("GIT_URI");
        Assert.notNull(uri, "GIT_URI is required");
        String username = dotenv.get("GIT_USERNAME");
        Assert.notNull(username, "GIT_USERNAME is required");
        String password = dotenv.get("GIT_PASSWORD");
        Assert.notNull(password, "GIT_PASSWORD is required");
        // Run testCloneAndPull() in JgitStarterApplicationTests.java to clone the repo first
        String path = "repo/" + pathOfUri(uri);
        Git git = Git.open(new File(path));
        ObjectId head = git.getRepository().resolve("HEAD");
        Assert.notNull(head, "HEAD is required");
        Repository repository = git.getRepository();
        git.getRepository().readOrigHead();
        BuiltinLFS.register();
        BuiltinLFS builtinLFS = (BuiltinLFS) LfsFactory.getInstance();
        Assert.isTrue(builtinLFS.isAvailable(), "LFS support is available");
        LfsFactory.LfsInstallCommand installCommand = new InstallBuiltinLfsCommand();
        installCommand.setRepository(git.getRepository());
        installCommand.call();
        Assert.isTrue(builtinLFS.isEnabled(git.getRepository()), "LFS support is enabled");
        String[] track = new String[] { "lfs", "track", "*.rar" };
        ProcessBuilder builder = FS.DETECTED.runInShell("git", track);
        if (repository != null) {
            builder.directory(repository.isBare() ? repository.getDirectory()
                    : repository.getWorkTree());
        }
        FS.DETECTED.runProcess(builder, System.out, null, (String) null);
    }
}

