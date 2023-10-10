package kr.mooner510.commitspammer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class Pusher {
    private final BufferedWriter console;

    public Pusher(final List<Map.Entry<String, String>> urls) {
        console = new BufferedWriter(new OutputStreamWriter(System.out));
        urls.parallelStream().forEach(this::CreateNewTask);
    }

    private static void execute(BufferedWriter writer, String... cmd) {
        try {
            for (String s : cmd) {
                writer.write(s + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CreateNewTask(final Map.Entry<String, String> url) {
        Thread.startVirtualThread(() -> {
            File gitDir = new File(Config.repoPath + "/" + url.getKey() + "/.git");
            CredentialsProvider cp = new UsernamePasswordCredentialsProvider(Config.userName, Config.token);
            try (Git git = Git.open(gitDir)) {
//                git.branchCreate().setName("tmp").call();
//                git.merge().include(git.getRepository().findRef("tmp")).call();
//                System.out.println(git.branchList().call().stream().map(Ref::getName).toList());
                git.push().setPushAll().setCredentialsProvider(cp).call();
//                git.checkout().setName("refs/heads/master").setForced(true).call();
//                git.branchDelete().setBranchNames("tmp").setForce(true).call();
                execute(console, url.getKey() + ": Push Complete");
            } catch (GitAPIException | IOException e) {
                e.printStackTrace();
            }
        });
    }
}
