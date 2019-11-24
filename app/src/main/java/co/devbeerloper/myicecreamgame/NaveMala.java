package co.devbeerloper.myicecreamgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
public class NaveMala {

    public static final int SPRITE_SIZE_WIDTH =100;
    public static final int SPRITE_SIZE_HEIGTH=100;
    public static final float GRAVITY_FORCE=10;
    private final int MIN_SPEED = 1;
    private final int MAX_SPEED = 20;
    private final int image1 = R.drawable.nave_mala;
    private float maxY;
    private float maxX;
    private float width;
    private float heigth;


    private float speed = 0;
    private float positionX;
    private float positionY;
    private Bitmap spriteAsteroid;
    private boolean dead = false;

    public NaveMala(Context context, float screenWidth, float screenHeigth, int speed, float y){
        this.speed = speed;
        positionX = screenWidth;
        positionY = y;

        this.width = SPRITE_SIZE_WIDTH;
        this.heigth = SPRITE_SIZE_HEIGTH;
        //Getting bitmap from resource
        Bitmap originalBitmap= BitmapFactory.decodeResource(context.getResources(), image1);
        spriteAsteroid  = Bitmap.createScaledBitmap(originalBitmap, SPRITE_SIZE_WIDTH , SPRITE_SIZE_HEIGTH , false);

        this.maxX = screenWidth - (spriteAsteroid.getWidth()/2.0f);
        this.maxY = screenHeigth - spriteAsteroid.getHeight();
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    public Bitmap getSprite() {
        return spriteAsteroid;
    }

    public void setSprite(Bitmap spriteIcecreamCar) {
        this.spriteAsteroid = spriteIcecreamCar;
    }

    public Rectangle getRectangle(){
        return new Rectangle(this.positionX, this.positionY, this.width, this.heigth);
    }

    /**
     * Control the position and behaviour of the icecream car
     * @param level
     * @param h
     * @param freeze
     */
    public void updateInfo(float level, float h, boolean freeze) {
        this.positionX -= (freeze ? 5: speed*level);
        if (positionX < 0) {
            this.dead = true;

        }
    }

    public void kill(float y) {
        this.positionX = this.maxX;
        this.positionY = y;
        this.dead = false;
    }

    public boolean isDead() {
        return dead;
    }
}