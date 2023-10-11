<div align="center">
  <h1 align="center">Commit Spammer</h1>

  <p align="center">
    Spam a lot of dummy-commits!
    <br />
    <a href="https://github.com/Mooner510/CommitSpammer/issues"><strong>≪ Report Bugs ≫</strong></a>
  </p>
<p align="center">Requires <strong>Java 21</strong> or higher to run</p>
</div>

_Give me star if you like this project_

## Getting Started
### 1. Fork or Clone this Repository
You can clone this repository as a .zip file [here](https://github.com/Mooner510/CommitSpammer/archive/refs/heads/master.zip).

Or, you can clone it using your IDE or another git program.

### 2. Generate Github Token
You can generate new github token [here](https://github.com/settings/personal-access-tokens/new)

Remember to copy the generated token. Do not close the window before copying.

### 3. Edit Config Class
You have to edit Config Class.
If you know how to use java, edit the Config class to you want.

If you don't know how to use java, just input requirements between " symbols.

Use the IDE of your choice. You can also use notepad.

#### Example
- Github Username: Mooner510
- Github Email: mooner@mooner.com
- Directory: ./repo
- Github Token: "ghp_abcdefghijklmnopqrstuvwxyz"
- Repositories: [[dummy-commit-3](https://github.com/Mooner12/dummy-commit-3), [dummy-commit-4](https://github.com/Mooner12/dummy-commit-4)]

```java
package kr.mooner510.commitspammer;

import java.util.List;

public class Config {
    public static final String userName = "Mooner510"; // TODO: Enter your github username
    public static final String email = "mooner@mooner.com"; // TODO: Enter your github email
    public static final String repoPath = "./repo"; // TODO: Enter your root repository directory path
    public static final String token = "ghp_abcdefghijklmnopqrstuvwxyz"; // TODO: Enter your github token
    public static final List<String> remotes = List.of( // TODO: Enter your name of repositories for dummy-commit
            "dummy-commit-3",
            "dummy-commit-4"
    );
}
```

### 4. Build and Run
Create new class for run. There is an **example**.

#### Make 20000 commit using all repositories without cloning repositories:

```java
import kr.mooner510.commitspammer.Commiter;

public class Main {
    public static void main(String[] args) {
        new Commiter(Config.remotes, false, 20000);
    }
}
```

#### Clone all repositories and make 60000 commit using all repositories:

```java
import kr.mooner510.commitspammer.Commiter;

public class Main {
    public static void main(String[] args) {
        new Commiter(Config.remotes, true, 60000);
    }
}
```

#### Push all commits made by the Commiter class to all repositories every 20 seconds:

```java
import kr.mooner510.commitspammer.Pusher;

public class Main {
    public static void main(String[] args) {
        new Pusher(Config.remotes, 20000);
    }
}
```

### Enjoy!
