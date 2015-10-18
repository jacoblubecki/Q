# Q: Playback for Any File or Format

This project is a plain Java library that allows for the simple implementation of media players for a wide variety of
sources. For each format, create an object that extends the abstract Player class and associate it with a regex that
matches the URI of the format that player should handle.

## Note

This project is still very much a beta concept. I want to add more features (preload next track, crossfade, etc.) and
I've only barely tested the code so I can't guaranty it will work. This is open source so I welcome contributions, but I
will definitely expect good documentation and quality of code. Try to stick to existing code style, naming conventions,
etc. For an example of the project on Android, checkout the [Example Project](https://github.com/lubecjac/Q-Example).

## Features

- Basic playback (play, pause, previous, next, select track)
- Looping and shuffling
  - Loop nothing, a list of tracks, or a single track
  - Shuffle and start playing a random track or start the shuffled list on the current track
  - Reset to the original list of tracks
- Seeking

## Building
This project is built using [Gradle](https://gradle.org/):

1. Clone the repository: `git clone https://github.com/lubecjac/Q.git`
2. Open and build the project.
3. Grab the `jar` that can be found in `q/build/libs/q-0.1.1.jar` and include it in your application

Alternatively, the project can be imported into an existing project as a module.

## Usage

#### To populate the Q and prepare it for playback:

```java
// Setup
  PlaverEventCallback callback = ... ;

  Player webAudioPlayer = new WebAudioPlayer(callback);
  Player localAudioPlayer = new LocalAudioPlayer(callback);

  List<QTrack> tracks = new ArrayList<>();

  Track track = new Track();
  track.title = "My Track":
  track.artist = "My Artist";
  track.uri = "file://storage/music/My Song.mp3";
  track.image = "file://storage/images/My Song/albumart.jpg";

  tracks.add(track);

  // ... add more tracks ...

  QEventListener listener = ... ;

  Q music = Q.getInstance();
  music.setListener(listener);
  music.addPlayer("(http|https)://(.*).(mp3|wav)", webAudioPlayer);
  music.addPlayer("file://(.*).(mp3|wav)", localAudioPlayer);
  music.setTrackList(tracks);

  // play / pause / etc

```

#### Creating a player:

```java
public class WebAudioPlayer extends Player {

  private final MediaPlayer player;

  public WebAudioPlayerSimple(PlayerEventCallback callback) {
    super(callback);
    player = new MediaPlayer();
  }

  @Override public void prepare(String uri) {
    player.setDataSource(s);

    // When prepareAsync finishes
    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mediaPlayer) {
        notifyIfPrepared();
      }
    });

    // When audio playback finishes
    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mediaPlayer) {
        notifyIfTrackEnded();
      }
    });

    changeState(PlayerState.PREPARING);
    player.prepareAsync();
  }

  @Override public void justPrepare(String s) {
    player.setDataSource(s);

    // When prepareAsync finishes
    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mediaPlayer) {
        changeState(PlayerState.PAUSED);
      }
    });

    // When audio playback finishes
    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mediaPlayer) {
        notifyIfTrackEnded();
      }
    });

    changeState(PlayerState.PREPARING);
    player.prepareAsync();
  }

  @Override public void seekTo(int i) {
    player.seekTo(i);
  }

  @Override public int getCurrentTime() {
    return player.getCurrentTime();
  }

  @Override public int getDuration() {
    return player.getDuration();
  }

  @Override public void play() {
    super.play();
    player.start();
  }

  @Override public void pause() {
    super.pause();
    player.pause();
  }

  @Override public void stop() {
    super.stop();
    player.stop();
  }

  @Override public void release() {
    super.release();
    player.release();
  }
}
```

## Logging

The library is built to support any logging mechanism. By default, nothing will be logged.

#### To start logging

```java
QLog.setLogger(new AndroidLog());
QLog.setLogLevel(LogLevel.BASIC);
```

#### Customizing the logging mechanism

The easiest way is to implement the `Logger` interface or extend the `DefaultLog`.

```java
public class AndroidLog extends DefaultLog {

  @Override public void log(int priority, String tag, String message) {
    switch (priority) {
      case QLog.VERBOSE:
        Log.v(tag, message);
        break;

      case QLog.DEBUG:
        Log.d(tag, message);
        break;

      case QLog.INFO:
        Log.i(tag, message);
        break;

      case QLog.WARN:
        Log.w(tag, message);
        break;

      case QLog.ERROR:
        Log.e(tag, message);
        break;

      case QLog.WTF:
        Log.wtf(tag, message);
        break;

      default:
        Log.i("DEFAULT_TAG", message);
    }
  }
}
```

#### Error Handling

The library will break itself quickly if states aren't handled properly or something is done that shouldn't be. There are several cases where an `IllegalStateException` is thrown intentionally so that a user can fix an issue before
it becomes problematic. If this is more irritating than helpful, most of the exceptions can be logged instead of being
thrown.

To utilize this behavior, when declaring log preferences:

```java
  QLog.ignoreIllegalStates(true);
```

## License

This project is distributed as open source software under the MIT License. Please see the LICENSE file for more info.

#### Bugs, Feature requests

Found a bug? Something that's missing? Feedback is an important part of improving the project, so
please [open an issue](https://github.com/lubecjac/Q/issues).

#### Code

Fork this project and start working on your own feature branch. When you're done, send a Pull Request
to have your suggested changes merged into the master branch by the project's collaborators.
Read more about the [GitHub flow](https://guides.github.com/introduction/flow/).
