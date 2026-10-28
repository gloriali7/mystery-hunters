# Mystery Hunters

An 8-bit style choose-your-path mystery game in Java (Swing, no extra libraries).

## Run it

Open `src/App.java` in VS Code and click **Run** above `main`.

| Key | What it does |
|-----|--------------|
| Arrows / W-S | Move through choices |
| Enter / Space | Pick, or skip the typing text |
| 1-5 | Pick a choice directly |
| Esc | Back to the case list (your progress is saved) |
| M | Sound on/off |

The mouse works too.

## Play it in a web browser

The web version uses [CheerpJ](https://cheerpj.com), which runs the Java game inside the browser.

1. Double-click `Build Web Version.command`. It makes a `web-dist` folder and `web-dist.zip`.
2. In AWS Amplify, choose **Deploy without Git** and upload `web-dist.zip`.

Run the build again after you change the code or a story, then upload the new zip.
In the browser, progress is saved in that browser only.

## How the game works

Pick a case from the title screen. You have 3 hearts. Bad choices cost a heart and flash the screen red.
Lose all 3 and it's game over. Find every clue, then accuse the right suspect to win.
Accuse without enough proof and you lose a heart.

Progress saves automatically after every scene. Solved cases get a ★ on the case list.
The save file is at `~/.mystery-hunters/save.properties`. Delete it to reset everything.

## Writing your own case

Each case is a plain text file in the `stories` folder. Cases are listed in file-name order,
so name them `3-something.txt`, `4-something.txt` and so on. The easiest way to start is to copy an existing one.

```
// Lines starting with // are notes to yourself.

TITLE: The Haunted Lighthouse
COVER: manor                            <- picture on the title screen
START: arrive                           <- the first scene
GAMEOVER: The ghost keeps its secret.   <- shown when you lose

CLUE: KEY = A rusty brass key           <- CLUE: ID = what the player sees

SCENE: arrive
ART: manor
Waves crash against the rocks. The lighthouse lamp flickers on and off.
Any lines under the scene are its story text.
> Climb the stairs -> stairs
> Search the rocks -> rocks (gives KEY)
> Swim for it! -> swim (bad)
> Open the locked door -> inside (needs KEY, else locked)

SCENE: inside
ART: emerald
ENDING: win
You solved it!
```

Choice options go in brackets at the end:
- `(bad)` costs a heart.
- `(gives KEY)` gives a clue. The choice hides itself once the clue has been found.
- `(needs KEY OTHER, else scenename)` only works with those clues. Without them, the player goes to `scenename` and loses a heart.
- You can combine options, like `(bad, gives KEY)`.

**Pictures you can use with `ART:`:** manor, foyer, study, kitchen, garden, guestroom, lineup, emerald,
museum, gallery, security, office, storage, museumlineup, mask.
To draw a new one, add a method to `PixelArt.java` and register it in the `ART` table at the top.

If you make a mistake, such as a choice leading to a scene that doesn't exist, text too long to fit, or a
misspelled clue, the game shows a message when it starts. The message names the file and line to fix.

## Code files

| File | What it does |
|------|--------------|
| `StoryLoader.java` | Reads the story `.txt` files |
| `Story.java` / `Scene.java` / `Choice.java` | A loaded case, and checks it for mistakes |
| `GamePanel.java` | Title menu, drawing the screen, typewriter text, keyboard/mouse |
| `PixelArt.java` | The picture for each location, plus the character sprites |
| `Sound.java` | Chiptune sound effects, generated as square waves in code |
| `SaveData.java` | Saving progress, solved cases and the sound setting |
| `GameState.java` | Hearts, clues found, and current scene |
| `PixelFont.java` / `Palette.java` | The blocky font and the 16 retro colours |
