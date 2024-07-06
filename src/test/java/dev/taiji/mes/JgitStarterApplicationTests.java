package dev.taiji.mes;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
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
    static String uri = "https://gitee.com/qiuzhanghua/learn-kotlin.git";

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

        if (new File("repo/learn-kotlin").exists()) {
            Git git = Git.open(new File("repo/learn-kotlin"));
            git.pull().setCredentialsProvider(cp).call();
            git.close();
            System.out.println("repo/learn-kotlin pulled");
        } else {
            Git clone = Git.cloneRepository()
                    .setURI(uri)
//				.setTransportConfigCallback(transport -> {
//					if (transport instanceof SshTransport) {
//						((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
//					}
//				})
                    .setCredentialsProvider(cp)
                    .setDirectory(new File("repo/learn-kotlin"))
                    .call();
            clone.close();
            System.out.println("repo/learn-kotlin cloned");
        }
    }
}
