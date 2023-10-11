package kr.mooner510.commitspammer;

import kr.mooner510.commitspammer.utils.Utils;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
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
    private final boolean init;
    private final AtomicInteger integer;

    public Commiter(final List<String> repositories, final boolean init, final int requirement) {
        integer = new AtomicInteger(requirement);
        this.init = init;
        repositories.parallelStream().map(Utils::toURLEntry).forEach(this::CreateNewTask);
        while (requirement == integer.get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Runtime.getRuntime().exit(404);
            }
        }
        Timer timer = new Timer();
        ProgressBar progressBar = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setInitialMax(requirement)
                .setTaskName("Commit")
                .build();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int n = integer.get();
                if (n <= 0) {
                    progressBar.close();
                    timer.cancel();
                    return;
                }
                progressBar.stepTo(requirement - n);
                progressBar.refresh();
            }
        }, 0, 500);
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
                    Thread.sleep(1000);
                }
                Thread.startVirtualThread(() -> {
                    int pa;
                    do {
                        pa = integer.getAndDecrement();
                        if (pa <= 0) break;
                        try (Git git = Git.open(gitDir)) {
                            git.commit().setMessage("dummy-commit").setCommitter(Config.userName, Config.email).setAuthor(Config.userName, Config.email).setCredentialsProvider(cp).call();
                        } catch (IOException | GitAPIException e) {
                            e.printStackTrace();
                            Runtime.getRuntime().exit(404);
                        }
                    } while (true);
                });
            } catch (InterruptedException | GitAPIException | URISyntaxException e) {
                e.printStackTrace();
                Runtime.getRuntime().exit(404);
            }
        });
    }
}
