package kr.mooner510.commitspammer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Pusher {
    private final BufferedWriter console;

    public Pusher(final List<Map.Entry<String, String>> urls) {
        console = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String s;
        while (true) {
            try {
                execute(console, "\nWait for next enter... ('stop' to exit)");
                s = reader.readLine();
                if (s.equals("stop")) {
                    reader.close();
                    break;
                }
                urls.parallelStream().forEach(this::StartNewTask);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public void StartNewTask(final Map.Entry<String, String> url) {
        File gitDir = new File(Config.repoPath + "/" + url.getKey() + "/.git");
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(Config.userName, Config.token);
        Thread.startVirtualThread(() -> {
            try (Git git = Git.open(gitDir)) {
//                git.branchCreate().setName("tmp").call();
//                git.merge().include(git.getRepository().findRef("tmp")).call();
//                System.out.println(git.branchList().call().stream().map(Ref::getName).toList());
                git.push().setForce(true).setPushAll().setCredentialsProvider(cp).call();
//                git.checkout().setName("refs/heads/master").setForced(true).call();
//                git.branchDelete().setBranchNames("tmp").setForce(true).call();
                execute(console, url.getKey() + ": Push Complete");
            } catch (GitAPIException | IOException e) {
                e.printStackTrace();
            }
        });
    }
}
