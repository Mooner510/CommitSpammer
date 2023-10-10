package kr.mooner510.commitspammer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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

public class Commiter {
    private final BufferedWriter console;
    private final boolean init;
    private final boolean pull;
    private final AtomicInteger integer;

    public Commiter(final List<Map.Entry<String, String>> urls, final boolean init, final boolean pull, final int requirement) {
        console = new BufferedWriter(new OutputStreamWriter(System.out));
        integer = new AtomicInteger(requirement);
        this.init = init;
        this.pull = pull;
        urls.parallelStream().forEach(this::CreateNewTask);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int i = integer.get();
                execute(console, "Commits [ " + i + " / " + requirement + " ] ( " + ((requirement - i) * 100d / requirement) + " )");
            }
        }, 1000, 1000);
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
            try {
                File dir = new File(Config.repoPath + "/" + url.getKey() + "/");
                File gitDir = new File(Config.repoPath + "/" + url.getKey() + "/.git");
                new File(Config.repoPath + "/" + url.getKey()).mkdirs();
                CredentialsProvider cp = new UsernamePasswordCredentialsProvider(Config.userName, Config.token);
                if (init) {
                    try (Git git = Git.cloneRepository()
                            .setDirectory(dir)
                            .setURI(url.getValue())
                            .setCredentialsProvider(cp)
                            .call()) {
                        git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish(url.getValue())).call();
                        git.checkout().setForced(true).setName("refs/heads/master").call();
                    }
                }
                if (!init && pull) {
                    try (Git git = Git.open(gitDir)) {
                        git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish(url.getValue())).call();
//                        git.fetch().call();
                        git.checkout().setForced(true).setName("origin/master").call();
//                        git.pull().call();
                    }
                }
                Thread.sleep(100);
                execute(console, url.getKey() + ": Enqueued\n");
                Thread.startVirtualThread(() -> {
                    int pa;
                    do {
                        pa = integer.getAndDecrement();
                        try (Git git = Git.open(gitDir)) {
                            git.commit().setMessage("dummy-commit").setCommitter(Config.userName, Config.email).setAuthor(Config.userName, Config.email).setCredentialsProvider(cp).call();
                        } catch (IOException | GitAPIException e) {
                            e.printStackTrace();
                        }
                    } while (pa > 0);
                });
            } catch (InterruptedException | GitAPIException | IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }
}
