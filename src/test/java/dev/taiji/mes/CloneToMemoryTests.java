package dev.taiji.mes;

import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

@SpringBootTest
public class CloneToMemoryTests {
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

        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        InMemoryRepository repo = new InMemoryRepository(repoDesc);
        Git git = new Git(repo);
        git.fetch()
                .setCredentialsProvider(cp)
                .setRemote(uri)
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
                .call();
        repo.getObjectDatabase();
        ObjectId lastCommitId = repo.resolve("refs/heads/" + "main");
        System.out.println(lastCommitId);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setFilter(PathFilter.create("README.md"));
        treeWalk.setRecursive(true);
        if (!treeWalk.next()) {
            return;
        }
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objectId);
        loader.copyTo(System.out);
    }

    @Test
    void testCloneWithSSH() {
        String uri = "git@gitee.com:qiuzhanghua/jgit-starter.git";
        uri = "git@github.com:qiuzhanghua/jgit-starter.git";
        File sshDir = new File(FS.DETECTED.userHome(), ".ssh");
        SshdSessionFactory sshdSessionFactory = new SshdSessionFactoryBuilder()
                .setPreferredAuthentications("publickey,keyboard-interactive,password")
                .setHomeDirectory(FS.DETECTED.userHome())
                .setSshDirectory(sshDir)
                .setConfigFile(file -> {
                    try {
                        // Set StrictHostKeyChecking to no for gitee.com/github.com
                        return ResourceUtils.getFile("classpath:ssh_config");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build(new JGitKeyCache());
        SshdSessionFactory.setInstance(sshdSessionFactory);
        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        InMemoryRepository repo = new InMemoryRepository(repoDesc);
        Git git = new Git(repo);
        try {
            git.fetch()
                    .setRemote(uri)
                    .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        InMemoryRepository.MemObjDatabase db = repo.getObjectDatabase();
        Arrays.stream(db.getCurrentPacks()).forEach(pack -> {
            System.out.println(pack);
        });

    }

}
