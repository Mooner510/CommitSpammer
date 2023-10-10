package kr.mooner510.commitspammer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Commiter {
    private final BufferedWriter console;
    private final boolean init;
    private final Timer timer;

    public Commiter(final List<Map.Entry<String, String>> urls, final boolean init) {
        console = new BufferedWriter(new OutputStreamWriter(System.out));
        this.timer = new Timer();
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
        try {
            File dir = new File(Config.repoPath + "/.git");
            CredentialsProvider cp = new UsernamePasswordCredentialsProvider(Config.userName, Config.token);
            if (init) {
                Git git = Git.init().setDirectory(dir).call();
                git.remoteAdd().setName("origin").setUri(new URIish(url.getValue())).call();
                git.close();
                Thread.sleep(1000);
            }
            Path path = Path.of(Config.repoPath, url.getKey(), "dummy.txt");
            execute(console, url.getKey() + ": Enqueued\n");
            AtomicInteger integer = new AtomicInteger();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int pa = integer.getAndIncrement();
                    boolean pap = pa % 2 == 0;
                    execute(console, url.getKey() + ": Run " + pa);
                    try {
                        if (pap) {
                            execute(console, url.getKey() + ": Delete New");
                            Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING).close();
                        } else {
                            execute(console, url.getKey() + ": Create New");
                            BufferedWriter file = Files.newBufferedWriter(path, StandardOpenOption.CREATE);
                            execute(file, "dummy");
                            file.close();
                        }
                        execute(console, url.getKey() + ": Wrote Complete\n");

                        try (Git git = Git.open(dir)) {
                            git.commit().setMessage("dummy-commit" + pa).setAuthor(Config.userName, Config.email).setCredentialsProvider(cp).call();
                        } catch (GitAPIException e) {
                            e.printStackTrace();
                        }
                        execute(console, url.getKey() + ": Commit/Push Complete\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1000);
        } catch (InterruptedException | GitAPIException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
