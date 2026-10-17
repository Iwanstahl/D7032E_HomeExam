# Exploding Kittens (re-engineered)

D7032E home exam 2026, Isak Wanstål (isawan-3).

## Requirements

- Java 21 or newer (tested with Java 25).
- No build tool is needed.

All commands below are run from this folder (`isawan-3/`).

## Compile

PowerShell (Windows):

```powershell
javac -d out (Get-ChildItem -Recurse src\main -Filter *.java).FullName
```

Bash (Linux, macOS, Git Bash):

```bash
javac -d out $(find src/main -name "*.java")
```

This compiles only the game. The tests need JUnit, see [Run the tests](#run-the-tests).

## Run the game

Start the server first. You play at the server's keyboard as player 1:

```
java -cp out ExplodingKittens <humans> <bots>
```

- `<humans>` is the number of human players, including you at the server.
- `<bots>` is the number of bots.
- The base game has 2 to 5 players in total.
- The server waits until the other humans have connected, then the game starts.

Then start one client for each other human player, in its own terminal or on another computer:

```
java -cp out ExplodingKittens <server IP>
```

Examples:

| command | game |
|---|---|
| `java -cp out ExplodingKittens 1 1` | you against 1 bot (no clients needed) |
| `java -cp out ExplodingKittens 2 1` | you, 1 network player and 1 bot |
| `java -cp out ExplodingKittens 127.0.0.1` | a client on the same computer as the server |

The server uses port 2048 like the original.

**Playing:** every question lists numbered options. Type the number and press Enter.
When someone plays a card that can be Noped, every player holding a Nope has 5 seconds to answer.
If you don't answer in time, the answer is "Don't Nope".

## Run the tests

The tests use JUnit, which is included as `lib/junit-platform-console-standalone-6.1.3.jar`.
Compile the game and the tests together, then run them:

PowerShell (Windows):

```powershell
javac -d out -cp lib\junit-platform-console-standalone-6.1.3.jar (Get-ChildItem -Recurse src -Filter *.java).FullName
java -jar lib\junit-platform-console-standalone-6.1.3.jar execute --class-path out --scan-class-path
```

Bash (Linux, macOS, Git Bash):

```bash
javac -d out -cp lib/junit-platform-console-standalone-6.1.3.jar $(find src -name "*.java")
java -jar lib/junit-platform-console-standalone-6.1.3.jar execute --class-path out --scan-class-path
```

JUnit prints every test as passed or failed, followed by a summary.
