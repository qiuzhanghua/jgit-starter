package dev.taiji.mes;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

@SpringBootTest
class JgitStarterApplicationTests {

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
    void testCloneAndPull() throws GitAPIException, IOException {
        String uri = dotenv.get("GIT_URI");
        Assert.notNull(uri, "GIT_URI is required");
        String username = dotenv.get("GIT_USERNAME");
        Assert.notNull(username, "GIT_USERNAME is required");
        String password = dotenv.get("GIT_PASSWORD");
        Assert.notNull(password, "GIT_PASSWORD is required");
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(username, password);

        // SSH not working for jgit now
        String privateKey = "/Users/q/.ssh/id_rsa";
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no"); // or "ask", "yes"
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
//				JSch.setConfig("ssh-rsa", "com.jcraft.jsch.jce.SignatureRSA");
                JSch jSch = super.createDefaultJSch(fs);
                jSch.addIdentity(privateKey);
                return jSch;
            }
        };

        String path = "repo/" + pathOfUri(uri);
        if (new File(path).exists()) {
            Git git = Git.open(new File(path));
            git.pull().setCredentialsProvider(cp).call();
            git.close();
            System.out.println(path + " pulled");
        } else {
            Git clone = Git.cloneRepository()
                    .setURI(uri)
//				.setTransportConfigCallback(transport -> {
//					if (transport instanceof SshTransport) {
//						((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
//					}
//				})
                    .setCredentialsProvider(cp)
                    .setDirectory(new File(path))
                    .call();
            clone.close();
            System.out.println(path + " cloned");
        }
    }


    @Test
    void testRead() throws IOException, GitAPIException {
        String uri = dotenv.get("GIT_URI");
        Assert.notNull(uri, "GIT_URI is required");
        String path = "repo/" + pathOfUri(uri);
        Git git = Git.open(new File(path));
        ObjectId head = git.getRepository().resolve("HEAD");
        git.getRepository().readOrigHead();
        if (head == null) {
            System.out.println("No commits");
            return;
        }
        git.log().all().call().forEach(commit -> {
            System.out.println(commit.getAuthorIdent().getName() + " " + commit.getAuthorIdent().getWhen() + " " + commit.getFullMessage());
        });
        git.close();
    }

    @Test
    void testAdd() throws IOException, GitAPIException {
        String uri = dotenv.get("GIT_URI");
        Assert.notNull(uri, "GIT_URI is required");
        Git git = Git.open(new File("repo/" + pathOfUri(uri)));
        if (!new File("repo/" + pathOfUri(uri) + "/tree-01").exists()) {
            new File("repo/" + pathOfUri(uri) + "/tree-01").mkdir();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Add tree-01").call();
        }
        String txtFile = "repo/" + pathOfUri(uri) + "/tree-01/test.txt";
        if (!new File(txtFile).exists()) {
            new File(txtFile).createNewFile();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Add test.txt").call();
        }
        git.close();
    }

    @Test
    void testPush() throws IOException, GitAPIException {
        String uri = dotenv.get("GIT_URI");
        Assert.notNull(uri, "GIT_URI is required");
        String username = dotenv.get("GIT_USERNAME");
        Assert.notNull(username, "GIT_USERNAME is required");
        String password = dotenv.get("GIT_PASSWORD");
        Assert.notNull(password, "GIT_PASSWORD is required");
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(username, password);
        Git git = Git.open(new File("repo/" + pathOfUri(uri)));
        Iterable<PushResult> pushes = git.push().setCredentialsProvider(cp).call();
        pushes.forEach(pushResult -> {
            System.out.println(pushResult.getMessages());
        });
        git.close();
    }

    static String pathOfUri(String uri) {
        String[] split = uri.split("/");
        return split[split.length - 1].replace(".git", "");
    }
}
