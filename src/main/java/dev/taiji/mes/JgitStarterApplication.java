package dev.taiji.mes;

import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        try (Git git = Git.open(new File("."))) {
            git.pull().call();
            Repository repository = git.getRepository();
            git.add().addFilepattern(".").call();

            // Stage all changed files, including deleted files, excluding new files
            git.add().addFilepattern(".").setUpdate(true).call();

            // and then commit the changes.
            git.commit()
                    .setMessage("Commit all changes including additions")
                    .call();


            Iterable<PushResult> results = git.push()
                    .setRemote("gitee")
                    .call();
            for (PushResult r : results) {
                for (RemoteRefUpdate update : r.getRemoteUpdates()) {
                    System.out.println("Having result: " + update);
                    if (update.getStatus() != RemoteRefUpdate.Status.OK && update.getStatus() != RemoteRefUpdate.Status.UP_TO_DATE) {
                        String errorMessage = "Push failed: " + update.getStatus();
                        throw new RuntimeException(errorMessage);
                    }
                }
            }
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
