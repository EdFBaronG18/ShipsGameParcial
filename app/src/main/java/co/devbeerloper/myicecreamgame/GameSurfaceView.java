package co.devbeerloper.myicecreamgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GameSurfaceView extends SurfaceView implements Runnable {

    private boolean isPlaying;
    private Ship ship;
    private ArrayList<Asteroid> asteroids;
    private ArrayList<Coin> coins;
    private ArrayList<Rasho> rashos;
    private ArrayList<NaveMala> malos;
    private ArrayList<Ligth> luces;
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder holder;
    private Thread gameplayThread = null;
    private long timeCoin;
    private long timeAsteroid;
    private long timeMalos;
    private long timePlayerShoot;
    private ArrayList<Long> timesMalosShoot;
    private Context context;
    private float screenWith;
    private float screenHeight;
    private boolean firstTime;


    private final int numberAsteroids = 10;
    private final int numberCoins = 20;
    private final int numberMalos = 15;
    private final int numberLuces = 50;

    private int limitAsteroids = 3;
    private int limitCoins = 20;
    private int limitMalos = 3;
    private int currentAsteroids;
    private int currentCoins;
    private int currentMalos;
    private boolean coinLoaded;
    private boolean asteroidLoaded;
    private boolean malosLoaded;
    private float randomSet[];
    private Random random;
    private Snow snow;
    private Heal heal;
    private boolean freeze;
    private long freezeTime;
    private int score;
    private int level;
    private int life;
    private int randomSize = 10000;
    private int randomIndex = 0;

    private double posibilitySnow = 0.99;
    private double posibilityHeal = 0.99;

    /**
     * Contructor
     *
     * @param context
     */
    public GameSurfaceView(Context context, float screenWith, float screenHeight) {
        super(context);

        // Start the game in pause
        firstTime = true;
        isPlaying = false;
        random = new Random(System.currentTimeMillis());
        timePlayerShoot = System.currentTimeMillis();

        snow = null;
        heal = null;

        this.asteroids = new ArrayList<Asteroid>();
        this.coins = new ArrayList<Coin>();
        this.rashos = new ArrayList<Rasho>();
        this.malos = new ArrayList<NaveMala>();
        this.timesMalosShoot = new ArrayList<>();
        this.luces = new ArrayList<>();


        asteroidLoaded = false;
        coinLoaded = false;
        malosLoaded = false;

        currentAsteroids = 0;
        currentCoins = 0;
        currentMalos = 0;

        this.context = context;
        this.screenWith = screenWith;
        this.screenHeight = screenHeight;

        randomSet = new float[randomSize];
        for(int i = 0; i < randomSet.length; i++)
            randomSet[i] = generateH();

        fillArrays();

        level = 0;
        score = 0;
        life = 100;


        ship = new Ship(context, screenWith, screenHeight);
        paint = new Paint();
        holder = getHolder();
    }

    private void fillArrays() {
        for(int i = 0; i < numberAsteroids; i++)
            asteroids.add(new Asteroid(context, screenWith, screenHeight, random.nextBoolean(), 5, getNextY()));
        for(int i = 0; i < numberCoins; i++)
            coins.add(new Coin(this.context, this.screenWith, this.screenHeight, getNextY()));
        for(int i = 0; i < numberMalos; i++){
            malos.add(new NaveMala(this.context, this.screenWith, this.screenHeight, 5, getNextY()));
            timesMalosShoot.add(System.currentTimeMillis());
        }
        for(int i = 0; i < numberLuces; i++){
            Ligth luz = new Ligth(this.context, this.screenWith, this.screenHeight, getNextY());
            luz.setSpeed(random.nextInt(15)+5);
            luces.add(luz);
        }
    }


    /**
     * Method implemented from runnable interface
     */
    @Override
    public void run() {
        while (isPlaying) {
            inflateObjects();
            updateInfo();
            paintFrame();
        }
    }

    private void inflateObjects() {
        if(snow == null && Math.random() > posibilitySnow){
            snow = new Snow(context, screenWith, screenHeight, getNextY());
        }
        if(heal == null && Math.random() > posibilityHeal){
            heal = new Heal(context, screenWith, screenHeight, getNextY());
        }
        if(!coinLoaded || !asteroidLoaded || !malosLoaded){
            long currentTime = System.currentTimeMillis();
            if(!coinLoaded && currentTime - timeCoin > 1000){
                currentCoins++;
                timeCoin = currentTime;
                if(currentCoins == limitCoins)
                    coinLoaded = true;

            }
            if(!asteroidLoaded && currentTime - timeAsteroid > 1500){
                timeAsteroid = currentTime;
                currentAsteroids++;
                if(currentAsteroids == limitAsteroids)
                    asteroidLoaded = true;
            }

            if(!malosLoaded && currentTime - timeMalos > 1500){
                timeMalos = currentTime;
                currentMalos++;
                if(currentMalos == limitMalos)
                    malosLoaded = true;
            }
        }

        long currentTime = System.currentTimeMillis();
        if(life != 0 && currentTime - timePlayerShoot > 250){
            Rasho rasho = new Rasho(context, screenWith, screenWith, true, 10);
            rasho.setPositionX(ship.getPositionX()+ship.SPRITE_SIZE_WIDTH);
            rasho.setPositionY(ship.getPositionY()+ship.SPRITE_SIZE_HEIGTH/2.0f - rasho.SPRITE_SIZE_HEIGTH/2.0f);
            rashos.add(rasho);
            timePlayerShoot = System.currentTimeMillis();
        }

        for(int i = 0; i < currentMalos; i++){
            Long time = timesMalosShoot.get(i);
            NaveMala e = malos.get(i);
            if(currentTime - time > 1500){
                Rasho rasho = new Rasho(context, screenWith, screenWith, false, 10);
                rasho.setPositionX(e.getPositionX()-rasho.SPRITE_SIZE_WIDTH);
                rasho.setPositionY(e.getPositionY()+e.SPRITE_SIZE_HEIGTH/2.0f - rasho.SPRITE_SIZE_HEIGTH/2.0f);
                rashos.add(rasho);
                timesMalosShoot.set(i, System.currentTimeMillis());
            }
        }

    }
    private void updateInfo() {
        if(life != 0)
            ship.updateInfo();
        else{
            ship.setPositionX(-5);
        }
        // SNOW
        if(snow != null){
            snow.updateInfo();
            if(snow.isDead()) snow = null;
            else{
               if(isIntersect(ship.getRectangle(), snow.getRectangle())){ // SET POWER-UP
                   snow = null;
                   freeze = true;
                   freezeTime = System.currentTimeMillis();
               }
            }
        }

        if(heal != null){
            heal.updateInfo();;
            if(heal.isDead()) heal = null;
            else{
                if(isIntersect(ship.getRectangle(), heal.getRectangle())){
                    heal = null;
                    life = 100;
                }
            }
        }

        for(Ligth e: luces)
            e.updateInfo();

        if(System.currentTimeMillis() - freezeTime > 5000){
            freeze = false;
        }

        //RASHO
        for(int i = 0; i < rashos.size(); i++){
            Rasho e  = rashos.get(i);
            if(e.ver)
                e.updateInfo();
            else{
                e.updateInfo((float)(1.0 + level*0.5), freeze);
                if(isIntersect(ship.getRectangle(), e.getRectangle())){
                    rashos.remove(i--);
                    score -= 15;
                    life  -= 15;
                    if(life < 0) life = 0;
                }
            }
        }

        // ASTEROIDS
        for(int i = 0; i < currentAsteroids; i++){
            Asteroid e = asteroids.get(i);
            e.updateInfo((float)(1.0 + level*0.5), getNextY(), freeze);
            if(e.isDead()) {
                e.kill(getNextY());
                score -= 15;
            }
            else if(isIntersect(ship.getRectangle(), e.getRectangle())){
                e.kill(getNextY());
                score -= 15;
                life = 0;
            }
            else{
                for(int j = 0; j < rashos.size(); j++){
                    Rasho XD = rashos.get(j);
                    if(!XD.ver) continue;
                    if(isIntersect(XD.getRectangle(), e.getRectangle())){
                        score += 15;
                        if(score % 600 == 0){
                            level++;
                            limitAsteroids++;
                            asteroidLoaded = false;
                        }
                        e.kill(getNextY());
                        rashos.remove(j);
                        break;
                    }
                }
            }
        }

        //MALOS
        for(int i = 0; i < currentMalos; i++){
            NaveMala e = malos.get(i);
            e.updateInfo((float)(1.0 + level*0.5), getNextY(), freeze);
            if(e.isDead()) {
                e.kill(getNextY());
                score -= 15;
            }else if(isIntersect(ship.getRectangle(), e.getRectangle())){
                e.kill(getNextY());
                score -= 15;
                life = 0;
            }
            else{
                for(int j = 0; j < rashos.size(); j++){
                    Rasho XD = rashos.get(j);
                    if(!XD.ver) continue;
                    if(isIntersect(XD.getRectangle(), e.getRectangle())){
                        score += 15;
                        if(score % 600 == 0){
                            level++;
                            limitAsteroids++;
                            asteroidLoaded = false;
                        }
                        e.kill(getNextY());
                        rashos.remove(j);
                        break;
                    }
                }
            }
        }

        // COINS
        for(int i = 0; i < currentCoins; i++){
            Coin e = coins.get(i);
            if(!e.isDead()){
                e.updateInfo(getNextY());
                if(isIntersect(ship.getRectangle(), e.getRectangle())){ // ADD SCORE
                    score += 5;
                    if(score % 600 == 0){
                        e.setDead(true);
                        level++;
                        limitAsteroids++;
                        asteroidLoaded = false;
                    }
                    e.kill(getNextY());
                }
            }
        }


    }

    private float getNextY() {
        randomIndex %= randomSize;
        return randomSet[randomIndex++];
    }

    private float generateH() {
        return random.nextInt((int)screenHeight-100);
    }

    private boolean isIntersect(Rectangle a, Rectangle b) {
        return a.intersect(b) || b.intersect(a);
    }

    private void paintFrame() {
        float x, y;
        Paint paint;
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            for(Ligth e: luces)
                canvas.drawBitmap(e.getSprite(), e.getPositionX(), e.getPositionY(), this.paint);
            if(life != 0)
                canvas.drawBitmap(ship.getSpriteIcecreamCar(), ship.getPositionX(), ship.getPositionY(), this.paint);
            for(int i = 0; i < currentAsteroids; i++){
                Asteroid e = asteroids.get(i);
                canvas.drawBitmap(e.getSprite(), e.getPositionX(), e.getPositionY(), this.paint);
            }
            for(int i = 0; i < currentMalos; i++){
                NaveMala e = malos.get(i);
                canvas.drawBitmap(e.getSprite(), e.getPositionX(), e.getPositionY(), this.paint);
            }


            for(int i = 0; i < currentCoins; i++){
                Coin e = coins.get(i);
                if(!e.isDead())
                    canvas.drawBitmap(e.getSprite(), e.getPositionX(), e.getPositionY(), this.paint);
            }

            for(Rasho e: rashos)
                    canvas.drawBitmap(e.getSprite(), e.getPositionX(), e.getPositionY(), this.paint);


            if(snow != null)
                canvas.drawBitmap(snow.getSprite(), snow.getPositionX(), snow.getPositionY(), this.paint);
            if(heal != null && life != 0)
                canvas.drawBitmap(heal.getSprite(), heal.getPositionX(), heal.getPositionY(), this.paint);
            if(freeze){
                x = screenWith / 2;
                y = 80;

                paint = new Paint();
                paint.setTextSize(80);
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);

                canvas.drawText("FREEZE", x, y, paint);
                x = screenWith / 2;
                y = 160;

                paint = new Paint();
                paint.setTextSize(80);
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);

                double res = (5000.0 - (System.currentTimeMillis()-freezeTime))/1000.0;
                if(res < 0) res = 0;
                canvas.drawText(String.format("%.2f", res), x, y, paint);
            }

            x = 150;
            y = 50;

            paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Puntaje: " + score, x, y, paint);

            x = 550;
            y = 50;

            paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Vida: " + life + "%", x, y, paint);

            if(life == 0){
                x = 900;
                y = 50;

                paint = new Paint();
                paint.setTextSize(50);
                paint.setColor(Color.RED);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("PERDIO", x, y, paint);
            }

            x = screenWith - 500;
            y = 50;

            paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText("Nivel: " + level, x, y, paint);
            holder.unlockCanvasAndPost(canvas);
        }
    }


    public void pause() {
        isPlaying = false;
        try {
            gameplayThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void resume() {

        isPlaying = true && !firstTime;
        gameplayThread = new Thread(this);
        gameplayThread.start();
    }

    /**
     * Detect the action of the touch event
     *
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if(life == 0) return true;
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                System.out.println("TOUCH UP - STOP JUMPING");
                ship.setJumping(false);
                break;
            case MotionEvent.ACTION_DOWN:
                System.out.println("TOUCH DOWN - JUMP");
                ship.setJumping(true);
                break;
        }
        if(firstTime){
            firstTime = false;
            timeCoin = System.currentTimeMillis();
            timeMalos = timeAsteroid = timeCoin;
            resume();
        }
        return true;
    }

}
