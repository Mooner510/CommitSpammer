package kr.mooner510.commitspammer;

import kr.mooner510.commitspammer.utils.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static kr.mooner510.commitspammer.utils.Utils.execute;

public class Pusher {
    private final BufferedWriter console;
    private final long delay;

    public Pusher(final List<String> repositories, final long delay) {
        console = new BufferedWriter(new OutputStreamWriter(System.out));
        this.delay = delay;
        repositories.parallelStream().map(Utils::toURLEntry).forEach(this::StartNewTask);
    }

    public void StartNewTask(final Map.Entry<String, String> url) {
        Timer timer = new Timer();
        File gitDir = new File(Config.repoPath + "/" + url.getKey() + "/.git");
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(Config.userName, Config.token);
        Thread.startVirtualThread(() -> {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try (Git git = Git.open(gitDir)) {
                        git.push().setForce(true).setPushAll().setCredentialsProvider(cp).call();
                        execute(console, url.getKey() + ": Push Complete");
                    } catch (GitAPIException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }, delay, delay);
        });
    }
}
