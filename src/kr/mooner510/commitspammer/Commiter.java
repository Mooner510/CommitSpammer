package kr.mooner510.commitspammer;

import kr.mooner510.commitspammer.utils.Utils;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static kr.mooner510.commitspammer.utils.Utils.execute;

public class Commiter {
    private final BufferedWriter console;
    private final boolean init;
    private final AtomicInteger setup;
    private final AtomicInteger integer;

    public Commiter(final List<String> repositories, final boolean init, final int requirement) {
        integer = new AtomicInteger(requirement);
        console = new BufferedWriter(new OutputStreamWriter(System.out));
        this.init = init;
        setup = new AtomicInteger(repositories.size());
        execute(console, "Ready for Commiter...");
        repositories.parallelStream().map(Utils::toURLEntry).forEach(this::Setup);
        while (setup.get() > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Runtime.getRuntime().exit(404);
            }
        }
        execute(console, "\nCommiter is ready for start. Preparing...");
        repositories.parallelStream().map(Utils::toURLEntry).forEach(this::CreateNewTask);
        Timer timer = new Timer();
        execute(console, "ProgressBar Creation Complete.\n");
        ProgressBar progressBar = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setInitialMax(requirement)
                .setTaskName("Commit")
                .showSpeed()
                .setSpeedUnit(ChronoUnit.SECONDS)
                .build();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int n = integer.get();
                if (n <= 0) {
                    progressBar.stepTo(requirement);
                    progressBar.refresh();
                    progressBar.close();
                    timer.cancel();
                    execute(console, String.format("\n\t>> Done! %d commits created.\n", requirement));
                    Runtime.getRuntime().exit(200);
                    return;
                }
                progressBar.stepTo(requirement - n);
                progressBar.refresh();
            }
        }, 0, 500);
    }

    public void Setup(final Map.Entry<String, String> url) {
        execute(console, String.format("Repository Setup: %s", url.getKey()));
        Thread.startVirtualThread(() -> {
            File dir = new File(Config.repoPath + "/" + url.getKey() + "/");
            new File(Config.repoPath + "/" + url.getKey()).mkdirs();
            CredentialsProvider cp = new UsernamePasswordCredentialsProvider(Config.userName, Config.token);
            if (init) {
                execute(console, String.format("Cloning Repository: %s from %s", url.getKey(), url.getValue()));
                try {
                    try (Git git = Git.cloneRepository()
                            .setDirectory(dir)
                            .setURI(url.getValue())
                            .setCredentialsProvider(cp)
                            .call()) {
                        git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish(url.getValue())).call();
                        git.checkout().setForced(true).setName("refs/heads/master").call();
                    }
                } catch (URISyntaxException e) {
                    execute(console, String.format("\n\t>> Wait. I think this URL (%s) is incorrect. How about asking Google?", url.getValue()));
                    Runtime.getRuntime().exit(404);
                    return;
                } catch (GitAPIException e) {
                    execute(console, String.format("\n\t>> Well, does a repository named %s actually exist?", url.getKey()));
                    Runtime.getRuntime().exit(404);
                    return;
                } catch (JGitInternalException e) {
                    execute(console, String.format("\n\t>> Hey! Check if you have already cloned the repository named %s. How about setting 'init' variable to 'false'?", url.getKey()));
                    Runtime.getRuntime().exit(404);
                    return;
                }
                execute(console, String.format("Cloned Complete: %s from %s", url.getKey(), url.getValue()));
            }
            execute(console, String.format("Setup Complete: %s", url.getKey()));
            setup.getAndDecrement();
        });
    }

    public void CreateNewTask(final Map.Entry<String, String> url) {
        File gitDir = new File(Config.repoPath + "/" + url.getKey() + "/.git");
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(Config.userName, Config.token);
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
    }
}
