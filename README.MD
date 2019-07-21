
# JASGL - Java AWT/Swing Game Library
![Logo](img/logo.png)

Game Library purely written in Java by using AWT/Swing canvas rendering.
No external native libraries are used.

Start your game loop with a one liner (title, window size, background color).
This hides the following complexity: game loop thread, double-buffered 30 FPS active rendering on canvas, swing keyboard and mouse input processing and
Swing frame setup.
```java
GameLoopFrame.loop("Game Demo", 800, 450, Color.black, new MyGame());
```

Concentrate on your game containing the three major aspects:
input handling, game state update and rendering which is called each frame of 30 frames in a second.
```java
public class MyGame implements Game {

    /**
     * Store your game state using class attributes here.
     */

    /**
     * To access the game loop and change some aspects.
     * E.g.: use gameLoop.close() to close the game.
     */
    @Override
    public void init(GameLoop gameLoop) {

    }

    /**
     * Decide for a frame how input changes your game state.
     */
    @Override
    public void input(Keyboard keyboard, Mouse mouse) {

    }

    /**
     * Update the game state when given milliseconds are elapsed.
     */
    @Override
    public void update(double ms) {

    }

    /**
     * Render the frame of the current game state.
     */
    @Override
    public void render(Graphics2D g) {

    }

}
```

## Game Making Tutorial

Below some sections about game making and usage of this library.
If you have special requirements and can not reuse implemented classes,
it should always be possible to extend a library class.

### Load Images

![Images](img/images.png)

Usually games need images.
Put your images in maven's `/src/main/resources` folder and load the images in your
Game class.
This ensures that your images are shipped with the built jar.

```java
import javax.imageio.ImageIO;
import java.awt.Image;

private static final Image sprite1;
static {
    try {
        sprite1 = ImageIO.read(MyGame.class.getResourceAsStream("/path/to/sprite1.png"));
    } catch (IOException ex) {
        throw new RuntimeException(ex);
    }
}
```

If you need to access the image by attribute name, you could use reflection.
```java
public static Image getImageByName(String name) {
    try {
        Field f = MyGame.class.getDeclaredField(name);
        return (Image) f.get(null);
    } catch (Exception ex) {
        throw new RuntimeException(ex);
    }
}
```

### Sprite Sheet

![Spritesheet](img/spritesheet.png)

Skeleton-pack by [jesse-m.itch.io](https://jesse-m.itch.io/skeleton-pack)

Sprite sheets are used to put many images of a sprite into one image.
But then we have to state where the subimages are and what they animate.
A `SpriteSheet` gives a sequence of subimages a name (e.g. "killed" are the images where the skeleton collapses).

* *`SpriteSheet`* a sheet of image sprites.
  * `RpgMakerCharSpriteSheet` in rpg maker 8 character are in one sprite. This class automatically and correctly cuts the
    subimages.

### Sprite

![Sprite](img/sprite.png)

* *`Sprite`* is a moveable object in the game. Thus, it basically has a position. It is updatable and renderable.
  * `DummySprite` renders just a black rectangle.
  * `SpriteSheetSprite` is a sprite that uses a `SpriteSheet` and an `Animator` to present its
    appearance using animated sequence of images.
    * `RpgMakerCharSprite` is a rpg marker character sprite. You have to state the character index because
    in one sprite 8 character are present. The animator is given with a configured `SequentialFixedIntervalFrameAnimator`.
* `ListOfSprites` is a (array-)list of sprites with additional list based operations on sprites.

#### Interfaces

Sprites can implement interfaces when they have certain behavior.

* *`Pivotable`*: sprites have a direction (left,up,right,down) which can be changed. This can be used by a `Positioner`.


### Animator

![Spritesheet](img/animation.png)

An animator changes image frames to create an animation.

* *`Animator`*
  * *`FrameAnimator`* is an animator that changes frames (int).
    * `SequentialFixedIntervalFrameAnimator` is an frame animator that changes frames in a fixed time interval and in a given sequence (e.g. 1,5,2,4).

### Positioner

![Positioner](img/positioner.png)

A positioner moves `Sprite`s in the game, maybe based on input.

* *`Positioner`* changes positions of given sprites. Can also start and stop animation based on positioning (e.g. walking).
  * `RpgMakerCharPositioner` works like the one implemented in rpg maker. Moves the character left,up,right,down
     in a grid. While walking the correct animation of a `RpgMakerCharSprite` is played.
