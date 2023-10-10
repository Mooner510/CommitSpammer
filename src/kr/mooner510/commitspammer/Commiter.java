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
import java.util.concurrent.atomic.AtomicInteger;

public class Commiter {
    private final BufferedWriter console;
    private final boolean init;
    private final int requirement;
    private final AtomicInteger integer;

    public Commiter(final List<Map.Entry<String, String>> urls, final boolean init, final int requirement) {
        console = new BufferedWriter(new OutputStreamWriter(System.out));
        this.requirement = requirement;
        integer = new AtomicInteger(requirement);
        this.init = init;
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
        Timer timer = new Timer();
        Thread.startVirtualThread(() -> {
            try {
                File dir = new File(Config.repoPath + "/" + url.getKey() + "/.git");
                new File(Config.repoPath + "/" + url.getKey()).mkdirs();
                CredentialsProvider cp = new UsernamePasswordCredentialsProvider(Config.userName, Config.token);
                if (init) {
                    Git git = Git.init().setDirectory(dir).call();
                    git.remoteAdd().setName("origin").setUri(new URIish(url.getValue())).call();
                    git.close();
                }
                Thread.sleep(100);
                execute(console, url.getKey() + ": Enqueued\n");
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        try (Git git = Git.open(dir)) {
//                            execute(console, url.getKey() + ": Push Complete %%%%%%%%%%%%%%%%%%%%");
//                            git.push().setCredentialsProvider(cp).call();
//                        } catch (GitAPIException | IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, 60000, 60500);
                Thread.startVirtualThread(() -> {
                    int pa;
                    do {
                        pa = integer.getAndDecrement();
                        try (Git git = Git.open(dir)) {
                            git.commit().setMessage("dummy-commit").setCommitter(Config.userName, Config.email).setAuthor(Config.userName, Config.email).setCredentialsProvider(cp).call();
                            execute(console, url.getKey() + ": Commit Complete [ " + pa + " / " + requirement + " ]");
                        } catch (IOException | GitAPIException e) {
                            e.printStackTrace();
                        }
                    } while (pa > 0);
                });
            } catch (InterruptedException | GitAPIException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }
}
