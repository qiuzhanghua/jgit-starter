package dev.taiji.mes;

import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class JgitStarterApplication {

	static Dotenv dotenv;

	public static void main(String[] args) {
		dotenv = Dotenv
				.configure()
				.systemProperties()
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();
        String uri = dotenv.get("GIT_URI");
        String username = dotenv.get("GIT_USERNAME");
        String password = dotenv.get("GIT_PASSWORD");

        // for github.com LFS is not supported, but zip can be used
        // for gitee.com LFS is supported for enterprise users, upload is rejected for free users
//        try (Git git = Git.open(new File("."))) {
//            git.pull().call();
//            Repository repository = git.getRepository();
//            git.add().addFilepattern(".").call();
//
//            // Stage all changed files, including deleted files, excluding new files
//            git.add().addFilepattern(".").setUpdate(true).call();
//
//            // and then commit the changes.
//            git.commit()
//                    .setMessage("Commit all changes including additions")
//                    .call();
//
//            CredentialsProvider cp = new UsernamePasswordCredentialsProvider(username, password);
//            Iterable<PushResult> results = git.push()
//                    .setRemote(uri)
//                    .setCredentialsProvider(cp)
//                    .call();
//            for (PushResult r : results) {
//                for (RemoteRefUpdate update : r.getRemoteUpdates()) {
//                    System.out.println("Having result: " + update);
//                    if (update.getStatus() != RemoteRefUpdate.Status.OK && update.getStatus() != RemoteRefUpdate.Status.UP_TO_DATE) {
//                        String errorMessage = "Push failed: " + update.getStatus();
//                        throw new RuntimeException(errorMessage);
//                    }
//                }
//            }
//        } catch (IOException | GitAPIException e) {
//            throw new RuntimeException(e);
//        }
    }
}
