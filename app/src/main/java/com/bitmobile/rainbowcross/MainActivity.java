package com.bitmobile.rainbowcross;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitmobile.rainbowcross.R;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class MainActivity extends ActionBarActivity {
    private Integer verticalCount=2;
    private Integer horizontalCount=2;
    private Line[][]hLinesArray;
    private Line[][]vLinesArray;
    private Integer[][]hColorsArray;
    private Integer[][]vColorsArray;
    private Integer segmentLength=0;
    private Integer xWidth;
    private Integer xHeight;
    private Integer xx;
    private Integer yy;
    private Integer xOffset;
    private Integer yOffset;
    private TextView tvTimer;
    private LinearLayout mainLayout;
    private RelativeLayout rlCanvas;
    private AtomicBoolean animationHapen=new AtomicBoolean(false);
    private Handler timerHandler = new Handler();
    private Integer[] colors=new Integer[20];
    private Runnable timerRunnable;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private int maxLevel=1;
    private AtomicLong deltaTime=new AtomicLong(0);
    private AtomicLong time=new AtomicLong(0);
    private Menu menu;
    private AtomicBoolean sound_on_off=new AtomicBoolean(false);
    private MediaPlayer mPlayerWin;
    private MediaPlayer mPlayerHit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        colors[0]=Color.rgb(255,000,000);
        colors[1]=Color.rgb(000,000,255);
        colors[2]=Color.rgb(000,255,000);
        colors[3]=Color.rgb(255,255,000);
        colors[4]=Color.rgb(255,000,255);
        colors[5]=Color.rgb(128,128,128);
        colors[6]=Color.rgb(000,128,000);
        colors[7]=Color.rgb(128,000,000);
        colors[8]=Color.rgb(000,000,128);
        colors[9]=Color.rgb(128,128,000);
        colors[10]=Color.rgb(128,000,128);
        colors[11]=Color.rgb(192,192,192);
        colors[12]=Color.rgb(000,128,128);
        colors[13]=Color.rgb(000,000,000);
        colors[14]=Color.rgb(255,192,203);
        colors[15]=Color.rgb(255,165,0);
        colors[16]=Color.rgb(160,82,45);
        colors[17]=Color.rgb(160,82,45);
        colors[18]=Color.rgb(139,0,139);
        colors[19]=Color.rgb(255,239,213);
        mPlayerWin = MediaPlayer.create(this, R.raw.win);
        mPlayerHit = MediaPlayer.create(this, R.raw.hit);

        prefs = getApplicationContext().getSharedPreferences("RAINBOW_CROSS2", 0);
        editor = prefs.edit();
        sound_on_off.set(prefs.getBoolean("sound_on_off",false));
        loadArray();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if ((verticalCount != 0) && (horizontalCount != 0)) {
                    setContentView(R.layout.load_game_layout);
                    Button btnNewGame = (Button) findViewById(R.id.btnNewGame);
                    btnNewGame.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initLevelLayout();
                        }
                    });
                    Button btnContinueGame = (Button) findViewById(R.id.btnContinueGame);
                    btnContinueGame.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initMainLayout(false);
                        }
                    });
                } else {
                    initLevelLayout();
                }
            }
        },2500);
    }

    private void initLevelLayout(){
        setContentView(R.layout.select_level_layout);
        maxLevel=prefs.getInt("maxLevel",1);
        initLevelButton(R.id.btnLevel1,2);
        if (maxLevel>1){
            initLevelButton(R.id.btnLevel2,3);
        }
        if (maxLevel>2){
            initLevelButton(R.id.btnLevel3,4);
        }
        if (maxLevel>3){
            initLevelButton(R.id.btnLevel4,5);
        }
        if (maxLevel>4){
            initLevelButton(R.id.btnLevel5,6);
        }
        if (maxLevel>5){
            initLevelButton(R.id.btnLevel6,7);
        }
        if (maxLevel>6){
            initLevelButton(R.id.btnLevel7,8);
        }
        if (maxLevel>7){
            initLevelButton(R.id.btnLevel8,9);
        }
        if (maxLevel>8){
            initLevelButton(R.id.btnLevel9,10);
        }

    }

    private void initArrays(){
        hLinesArray=new Line[horizontalCount][horizontalCount+1];
        vLinesArray=new Line[verticalCount+1][verticalCount];
        hColorsArray=new Integer[horizontalCount][horizontalCount+1];
        vColorsArray=new Integer[verticalCount+1][verticalCount];
    }

    private void initLevelButton(int btnId, final int count){
        Button btnLevel=(Button) findViewById(btnId);
        btnLevel.setEnabled(true);
        btnLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verticalCount=count;
                horizontalCount=count;
                initMainLayout(true);
            }
        });
    }


    private void initMainLayout(boolean generateNewGrid){

        setContentView(R.layout.activity_main);
        mainLayout=(LinearLayout) findViewById(R.id.main_relative_layout);
        rlCanvas=(RelativeLayout) findViewById(R.id.rlCanvas);
        tvTimer=(TextView) mainLayout.findViewById(R.id.tvTimer);
        if (generateNewGrid){
            generateGrid();
            Random r = new Random();
            for (int i = 0; i < horizontalCount * verticalCount * 2; i++) {
                int j = r.nextInt(horizontalCount);
                int k = r.nextInt(verticalCount);
                rotateLines(j, k,false);
            }
            saveArrays();
        }
        drawGrid();
        startTimer();
    }

    private String MillsToString(Long millis){
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void startTimer(){
        if (timerRunnable!=null){
            timerHandler.removeCallbacks(timerRunnable);
        }
        final Long startTime = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime+deltaTime.get();
                tvTimer.setText(MillsToString(millis));
                timerHandler.postDelayed(this, 500);
                time.set(millis);
            }
        };
        timerRunnable.run();
    }

    private void checkWin(){
        boolean res=true;
        for (int i=0;i<horizontalCount+1;i++){
            int color=hColorsArray[0][i];
            for (int j=1;j<horizontalCount;j++){
                if (hColorsArray[j][i]!=color){
                    res=false;
                };
            }
        }
        for (int i=0;i<verticalCount+1;i++){
            int color=vColorsArray[i][0];
            for (int j=1;j<verticalCount;j++){
                if (vColorsArray[i][j]!=color){
                    res=false;
                }
            }
        }
        if (res){
            timerHandler.removeCallbacks(timerRunnable);
            if (sound_on_off.get()){
                mPlayerWin.start();
            }
            editor.putInt("hCount", 0);
            editor.putInt("vCount", 0);
            if ((maxLevel==prefs.getInt("maxLevel",1))&&(maxLevel==horizontalCount-1)) {
                editor.putInt("maxLevel",maxLevel+1);
                maxLevel++;
            }
            Long min= prefs.getLong("min_"+horizontalCount,0);
            editor.putLong("time",0);
            editor.commit();
            if ((time.get()<min)||(min==0)){
                editor.putLong("min_"+horizontalCount,time.get());
                editor.commit();
                LayoutInflater li = LayoutInflater.from(this);
                int leng;
                if (xWidth<xHeight){
                    leng=xWidth;
                }else{
                    leng=xHeight;
                }
                LinearLayout recordLayout= (LinearLayout) li.inflate(R.layout.record_layout,null);
                final PopupWindow recordPopUp=new PopupWindow(recordLayout,(int) Math.round(leng*0.85),(int)Math.round(leng*0.85));
                TextView tvHighScore=(TextView)recordLayout.findViewById(R.id.tvHighScore);
                tvHighScore.setText(MillsToString(time.get()));
                recordPopUp.showAtLocation(recordLayout, Gravity.CENTER,0,0);
                Button btnContinue =(Button)recordLayout.findViewById(R.id.btnCloseRecordPanel);
                btnContinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recordPopUp.dismiss();
                        initLevelLayout();
                    }
                });
            }
            initLevelLayout();
        }
    }

    private boolean saveArrays() {
        editor.putInt("hCount", horizontalCount);
        editor.putLong("time",0);
        for (int i=0;i<horizontalCount;i++){
            for (int j=0;j<horizontalCount+1;j++){
                editor.putInt("h_"+i+"_"+j,hColorsArray[i][j]);
            }
        }
        editor.putInt("vCount", verticalCount);
        for(int i=0;i<verticalCount+1;i++){
            for (int j=0;j<verticalCount;j++){
                editor.putInt("v_"+i+"_"+j,vColorsArray[i][j]);
            }
        }

        return editor.commit();
    }

    private void loadArray() {
        horizontalCount = prefs.getInt("hCount", 0);
        verticalCount = prefs.getInt("vCount", 0);
        deltaTime.set(prefs.getLong("time", 0));
        initArrays();
        for (int i=0;i<horizontalCount;i++){
            for (int j=0;j<horizontalCount+1;j++){
                hColorsArray[i][j]=prefs.getInt("h_"+i+"_"+j,0);
            }
        }
        for(int i=0;i<verticalCount+1;i++){
            for (int j=0;j<verticalCount;j++){
                vColorsArray[i][j]=prefs.getInt("v_"+i+"_"+j,0);
            }
        }
    }

    private void initMenuItem(boolean sound_on_off){
        MenuItem item = menu.findItem(R.id.sound_on_off);
        if (sound_on_off) {
            item.setTitle(R.string.sound_off);
        } else{
            item.setTitle(R.string.sound_on);
        }

    }

    private void setSegmentLength(){
        Display display = getWindowManager().getDefaultDisplay();
        xWidth = display.getWidth();
        xHeight = display.getHeight();
            if (xWidth / (horizontalCount + 1) > xHeight*0.6 / (verticalCount + 1)) {
                segmentLength = (int) Math.round(xHeight * 0.6 / (verticalCount + 1));
            } else {
                segmentLength = (int) Math.round(xWidth * 0.9 / (horizontalCount + 1));
            }
        xOffset=(xWidth-segmentLength*(horizontalCount))/2;
        yOffset=segmentLength/5;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu=menu;
        initMenuItem(sound_on_off.get());
        return true;
    }

    private void generateGrid(){
        initArrays();
        deltaTime.set(0);
        for (int i=0;i<horizontalCount;i++){
            for (int j=0;j<horizontalCount+1;j++){
                hColorsArray[i][j]=colors[j];
            }
        }
        for (int i=0;i<verticalCount+1;i++){
            for (int j=0;j<verticalCount;j++){
                vColorsArray[i][j]=colors[horizontalCount+i+1];
            }
        }
    }

    private void drawLines(){
        for(int i=0;i<horizontalCount;i++){
            for (int j=0;j<horizontalCount+1;j++){
                Line line=new Line(this,hColorsArray[i][j],segmentLength,true,i,j,xOffset,yOffset);
                hLinesArray[i][j]=line;
                rlCanvas.addView(line,0);
            }
        }
        for(int i=0;i<verticalCount+1;i++){
            for (int j=0;j<verticalCount;j++){
                Line line=new Line(this,vColorsArray[i][j],segmentLength,false,i,j,xOffset,yOffset);
                vLinesArray[i][j]=line;
                rlCanvas.addView(line,0);
            }
        }
    }

    private void rotateLines(int x,int y,boolean save){
        int color=vColorsArray[x+1][y];
        int color2=hColorsArray[x][y+1];
        int color3=vColorsArray[x][y];
        vColorsArray[x+1][y]=hColorsArray[x][y];
        hColorsArray[x][y+1]=color;
        vColorsArray[x][y]=color2;
        hColorsArray[x][y]=color3;
        if (save) {
            editor.putInt("v_" + String.valueOf(x + 1) + "_" + y, vColorsArray[x + 1][y]);
            editor.putInt("h_" + String.valueOf(x) + "_" + String.valueOf(y + 1), hColorsArray[x][y + 1]);
            editor.putInt("v_" + String.valueOf(x) + "_" + y, vColorsArray[x][y]);
            editor.putInt("h_" + String.valueOf(x) + "_" + String.valueOf(y), hColorsArray[x][y]);
            editor.commit();
        }
    }
    private void invalidateLines(int x,int y){

        rlCanvas.removeView(hLinesArray[x][y]);
        Line line=new Line(this,hColorsArray[x][y],segmentLength,true,x,y,xOffset,yOffset);
        hLinesArray[x][y]=line;

        rlCanvas.removeView(hLinesArray[x][y+1]);
        Line line1=new Line(this,hColorsArray[x][y+1],segmentLength,true,x,y+1,xOffset,yOffset);
        hLinesArray[x][y+1]=line1;

        rlCanvas.removeView(vLinesArray[x][y]);
        Line line2=new Line(this,vColorsArray[x][y],segmentLength,false,x,y,xOffset,yOffset);
        vLinesArray[x][y]=line2;

        rlCanvas.removeView(vLinesArray[x+1][y]);
        Line line3=new Line(this,vColorsArray[x+1][y],segmentLength,false,x+1,y,xOffset,yOffset);
        vLinesArray[x+1][y]=line3;

        rlCanvas.addView(line2,0);
        rlCanvas.addView(line1,0);
        rlCanvas.addView(line,0);
        rlCanvas.addView(line3,0);
    }


    private void drawGrid(){
        rlCanvas.removeAllViews();
        setSegmentLength();
        drawLines();
        rlCanvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_DOWN) && (!animationHapen.get())) {
                    if (((event.getX() > xOffset) && (event.getX() < xOffset + segmentLength * horizontalCount)) && ((event.getY() > yOffset) && (event.getY() < yOffset + segmentLength * verticalCount))) {
                        if (sound_on_off.get()){
                            mPlayerHit.start();
                        }
                        xx = (int) Math.floor((event.getX() - xOffset) / segmentLength);
                        if (xx>horizontalCount-1){
                            xx=horizontalCount-1;
                        }
                        yy = (int) Math.floor((event.getY() - yOffset) / segmentLength);
                        if (yy>verticalCount-1){
                            yy=verticalCount-1;
                        }
                        RotateAnimation animation = new RotateAnimation(0, 90, Math.round((xx + 0.5) * segmentLength + xOffset), Math.round((yy + 0.5) * segmentLength + yOffset));
                        animation.setDuration(500);
                        animation.setFillAfter(true);
                        RotateAnimation animation1 = new RotateAnimation(0, 90, Math.round((xx + 0.5) * segmentLength + xOffset), Math.round((yy + 0.5) * segmentLength + yOffset));
                        animation1.setDuration(500);
                        hLinesArray[xx][yy].startAnimation(animation);
                        vLinesArray[xx][yy].startAnimation(animation);
                        hLinesArray[xx][yy + 1].startAnimation(animation);
                        vLinesArray[xx + 1][yy].startAnimation(animation1);

                        rotateLines(xx, yy, true);
                        animationHapen.set(true);
                        animation1.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                invalidateLines(xx, yy);
                                checkWin();
                                animationHapen.set(false);
                            }

                            @Override
                            public void onAnimationRepeat(Animation    animation) {

                            }
                        });

                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            editor.putLong("time",time.get());
            editor.commit();
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        if (id == R.id.action_new_game) {
            initLevelLayout();
            return true;
        }
        if (id == R.id.action_rate) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.bitmobile.rainbowcross2"));
            try{
                startActivity(intent);
            }catch (Exception e){
                try{
                    intent.setData(Uri.parse("https://play.google.com/store/apps/id=com.bitmobile.rainbowcross2"));
                } catch( Exception e1){
                    final Toast tst=Toast.makeText(this,"Could not open Android market, please install the market app.",Toast.LENGTH_SHORT);
                    tst.show();
                }
            }
            return true;
        }
        if (id == R.id.sound_on_off) {
            if (sound_on_off.get()){
                sound_on_off.set(false);
            }else{
                sound_on_off.set(true);
            }
            initMenuItem(sound_on_off.get());
            editor.putBoolean("sound_on_off", sound_on_off.get());
            editor.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (rlCanvas!=null) {
            rlCanvas.removeAllViews();
            drawGrid();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (timerRunnable!=null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timerRunnable!=null) {
            deltaTime.set(time.get());
            startTimer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor.putLong("time",time.get());
        editor.commit();
    }
}
