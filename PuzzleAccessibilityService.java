package com.fusen.autosolver;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.hardware.HardwareBuffer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.view.accessibility.AccessibilityEvent;

import java.util.*;
import java.util.concurrent.Executors;

public class PuzzleAccessibilityService extends AccessibilityService {
    private WindowManager wm; private Button auto; private final Handler main=new Handler(Looper.getMainLooper());
    private boolean busy=false;

    @Override public void onServiceConnected(){
        super.onServiceConnected();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        auto=new Button(this); auto.setText("AUTO"); auto.setTextSize(12); auto.setOnClickListener(v->runSolver());
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, -3);
        lp.gravity=Gravity.TOP|Gravity.END; lp.x=16; lp.y=110;
        wm.addView(auto,lp);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent e){}
    @Override public void onInterrupt(){}
    @Override public void onDestroy(){try{if(wm!=null&&auto!=null)wm.removeView(auto);}catch(Exception ignored){}super.onDestroy();}

    private void runSolver(){
        if(busy)return; busy=true; auto.setEnabled(false); auto.setText("...");
        if(Build.VERSION.SDK_INT<30){finish(false);return;}
        takeScreenshot(android.view.Display.DEFAULT_DISPLAY, Executors.newSingleThreadExecutor(), result->{
            Bitmap bmp=null;
            try{
                HardwareBuffer hb=result.getHardwareBuffer(); bmp=Bitmap.wrapHardwareBuffer(hb,result.getColorSpace());
                if(bmp==null){finish(false);return;}
                final Bitmap copy=bmp.copy(Bitmap.Config.ARGB_8888,false);
                main.post(()->{ List<PuzzleSolver.Move> moves=detectAndSolve(copy); if(moves==null||moves.isEmpty()){toast("No valid move detected");finish(false);return;} playMoves(moves,copy); });
            }catch(Exception ex){main.post(()->{toast("Screen read failed: "+ex.getClass().getSimpleName());finish(false);});}
            finally{try{result.getHardwareBuffer().close();}catch(Exception ignored){}}
        });
    }

    private List<PuzzleSolver.Move> detectAndSolve(Bitmap b){
        int W=b.getWidth(),H=b.getHeight(); int minX=W,maxX=0,minY=H,maxY=0;
        for(int y=(int)(H*.20);y<(int)(H*.65);y+=2)for(int x=(int)(W*.05);x<(int)(W*.95);x+=2){int p=b.getPixel(x,y);int r=Color.red(p),g=Color.green(p),bl=Color.blue(p);if(bl>150&&g>100&&r<110){minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}}
        if(maxX-minX<200||maxY-minY<200)return null;
        float cw=(maxX-minX)/9f,ch=(maxY-minY)/9f;
        boolean[][] board=new boolean[9][9];
        for(int r=0;r<9;r++)for(int c=0;c<9;c++){int x=(int)(minX+(c+.5)*cw),y=(int)(minY+(r+.5)*ch);int p=b.getPixel(Math.min(W-1,x),Math.min(H-1,y));board[r][c]=isFilled(p);}
        List<PuzzleSolver.Piece> pieces=detectPieces(b,minX,maxX,(int)(H*.73),(int)(H*.94),cw);
        if(pieces.size()<1)return null;
        return PuzzleSolver.solve(board,pieces);
    }
    private boolean isFilled(int p){int r=Color.red(p),g=Color.green(p),b=Color.blue(p);int mx=Math.max(r,Math.max(g,b)),mn=Math.min(r,Math.min(g,b));return mx>95 && (mx-mn)>30;}

    private List<PuzzleSolver.Piece> detectPieces(Bitmap b,int bx1,int bx2,int y1,int y2,float cell){
        int W=b.getWidth(),H=b.getHeight(); y1=Math.max(0,y1);y2=Math.min(H,y2); int step=Math.max(2,(int)(cell/8));
        boolean[][] m=new boolean[Math.max(1,(y2-y1)/step)][Math.max(1,(bx2-bx1)/step)];
        for(int yy=0;yy<m.length;yy++)for(int xx=0;xx<m[0].length;xx++){int x=bx1+xx*step,y=y1+yy*step;int p=b.getPixel(Math.min(W-1,x),Math.min(H-1,y));m[yy][xx]=isPiecePixel(p);}
        boolean[][] seen=new boolean[m.length][m[0].length]; List<PuzzleAccessibilityService.Box> boxes=new ArrayList<>();
        for(int y=0;y<m.length;y++)for(int x=0;x<m[0].length;x++)if(m[y][x]&&!seen[y][x]){int[] qx=new int[m.length*m[0].length],qy=new int[qx.length];int head=0,tail=0;qx[tail]=x;qy[tail++]=y;seen[y][x]=true;int ax=x,bx=x,ay=y,by=y,n=0;while(head<tail){int cx=qx[head],cy=qy[head++];n++;ax=Math.min(ax,cx);bx=Math.max(bx,cx);ay=Math.min(ay,cy);by=Math.max(by,cy);for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int nx=cx+d[0],ny=cy+d[1];if(nx>=0&&ny>=0&&nx<m[0].length&&ny<m.length&&m[ny][nx]&&!seen[ny][nx]){seen[ny][nx]=true;qx[tail]=nx;qy[tail++]=ny;}}}}
            if(n>20)boxes.add(new Box(ax,ay,bx,by,n));}
        boxes.sort(Comparator.comparingInt(o->o.ax)); List<PuzzleSolver.Piece> out=new ArrayList<>();
        for(Box z:boxes){float px=bx1+(z.ax+z.bx+1)*step/2f, py=y1+(z.ay+z.by+1)*step/2f;int w=Math.max(1,Math.round((z.bx-z.ax+1)*step/cell));int h=Math.max(1,Math.round((z.by-z.ay+1)*step/cell)); if(w>4||h>4)continue; List<int[]> cells=new ArrayList<>();
            // Reconstruct occupied cells from the component bounds by sampling cell centers.
            for(int ry=0;ry<h;ry++)for(int cx=0;cx<w;cx++){int sx=(int)(bx1+(z.ax+z.bx+1)*step/2f + (cx-(w-1)/2f)*cell);int sy=(int)(y1+(z.ay+z.by+1)*step/2f + (ry-(h-1)/2f)*cell);if(isPiecePixel(b.getPixel(Math.max(0,Math.min(W-1,sx)),Math.max(0,Math.min(H-1,sy)))))cells.add(new int[]{cx,ry});}
            if(!cells.isEmpty())out.add(new PuzzleSolver.Piece(cells,Math.round(px),Math.round(py)));
        }
        return out.size()>3?new ArrayList<>(out.subList(0,3)):out;
    }
    private boolean isPiecePixel(int p){int r=Color.red(p),g=Color.green(p),b=Color.blue(p);int mx=Math.max(r,Math.max(g,b)),mn=Math.min(r,Math.min(g,b));return mx>130&&(mx-mn)>45;}
    private static class Box{int ax,ay,bx,by,n;Box(int a,int c,int d,int e,int f){ax=a;ay=c;bx=d;by=e;n=f;}}

    private void playMoves(List<PuzzleSolver.Move> moves,Bitmap b){
        // Re-detect source positions and board geometry, then perform moves sequentially.
        int W=b.getWidth(),H=b.getHeight();int minX=W,maxX=0,minY=H,maxY=0;for(int y=(int)(H*.20);y<(int)(H*.65);y+=2)for(int x=(int)(W*.05);x<(int)(W*.95);x+=2){int p=b.getPixel(x,y);if(Color.blue(p)>150&&Color.green(p)>100&&Color.red(p)<110){minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}}
        float cw=(maxX-minX)/9f,ch=(maxY-minY)/9f;List<PuzzleSolver.Piece> pieces=detectPieces(b,minX,maxX,(int)(H*.73),(int)(H*.94),cw);
        long delay=100;for(PuzzleSolver.Move m:moves){if(m.piece>=pieces.size())continue;PuzzleSolver.Piece p=pieces.get(m.piece);float sx=p.sourceX,sy=p.sourceY;float tx=minX+(m.col+p.w()/2f)*cw,ty=minY+(m.row+p.h()/2f)*ch;dispatchDrag(sx,sy,tx,ty,delay);delay+=650;}
        main.postDelayed(()->finish(true),delay+200);
    }
    private void dispatchDrag(float sx,float sy,float tx,float ty,long delay){main.postDelayed(()->{Path path=new Path();path.moveTo(sx,sy);path.lineTo(tx,ty);GestureDescription.StrokeDescription stroke=new GestureDescription.StrokeDescription(path,0,450);dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(),null,null);},delay);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void finish(boolean ok){busy=false;auto.setEnabled(true);auto.setText("AUTO");if(ok)toast("Moves completed");}
}
