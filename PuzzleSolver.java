package com.fusen.autosolver;

import java.util.*;

public final class PuzzleSolver {
    public static final class Piece {
        public final List<int[]> cells; public final int sourceX, sourceY;
        Piece(List<int[]> c, int x, int y) { cells=c; sourceX=x; sourceY=y; }
        int h(){ int m=0; for(int[] c:cells)m=Math.max(m,c[1]+1); return m; }
        int w(){ int m=0; for(int[] c:cells)m=Math.max(m,c[0]+1); return m; }
    }
    public static final class Move { public int piece, row, col, score; Move(int p,int r,int c,int s){piece=p;row=r;col=c;score=s;} }

    public static List<Move> solve(boolean[][] start, List<Piece> pieces) {
        List<Move> out=new ArrayList<>(); boolean[][] board=copy(start);
        // Greedy look-ahead: choose the next move that maximizes line clears and future mobility.
        List<Integer> remaining=new ArrayList<>(); for(int i=0;i<pieces.size();i++) remaining.add(i);
        while(!remaining.isEmpty()) {
            Move best=null; boolean[][] bestBoard=null;
            for(int pi:remaining){ Piece p=pieces.get(pi); int h=p.h(), w=p.w();
                for(int r=0;r<=9-h;r++) for(int c=0;c<=9-w;c++) {
                    if(!fits(board,p,r,c)) continue;
                    boolean[][] b=copy(board); rawPlace(b,p,r,c); int before=0, after=countLines(b); clear(b);
                    int score=after*10000 + mobility(b,pieces,remaining,pi)*30 - roughness(b)*3 + (r==0?20:0);
                    if(best==null || score>best.score){best=new Move(pi,r,c,score); bestBoard=b;}
                }
            }
            if(best==null) break;
            out.add(best); board=bestBoard; remaining.remove((Integer)best.piece);
        }
        return out;
    }
    static int mobility(boolean[][] b,List<Piece> ps,List<Integer> rem,int skip){int n=0;for(int pi:rem)if(pi!=skip){Piece p=ps.get(pi);for(int r=0;r<=9-p.h();r++)for(int c=0;c<=9-p.w();c++)if(fits(b,p,r,c))n++;}return Math.min(n,200);}
    static int roughness(boolean[][] b){int n=0;for(int r=0;r<9;r++)for(int c=0;c<9;c++)if(b[r][c]){if(r<8&&!b[r+1][c])n++;if(c<8&&!b[r][c+1])n++;}return n;}
    static int countLines(boolean[][] b){int n=0;for(int r=0;r<9;r++){boolean ok=true;for(int c=0;c<9;c++)ok&=b[r][c];if(ok)n++;}for(int c=0;c<9;c++){boolean ok=true;for(int r=0;r<9;r++)ok&=b[r][c];if(ok)n++;}return n;}
    static void rawPlace(boolean[][] b,Piece p,int r,int c){for(int[] q:p.cells)b[r+q[1]][c+q[0]]=true;}
    static void clear(boolean[][] b){boolean[] rr=new boolean[9],cc=new boolean[9];for(int r=0;r<9;r++){rr[r]=true;for(int c=0;c<9;c++)rr[r]&=b[r][c];}for(int c=0;c<9;c++){cc[c]=true;for(int r=0;r<9;r++)cc[c]&=b[r][c];}for(int r=0;r<9;r++)for(int c=0;c<9;c++)if(rr[r]||cc[c])b[r][c]=false;}
    static boolean fits(boolean[][] b,Piece p,int r,int c){for(int[] q:p.cells)if(r+q[1]>=9||c+q[0]>=9||b[r+q[1]][c+q[0]])return false;return true;}
    static boolean[][] copy(boolean[][] a){boolean[][] b=new boolean[9][9];for(int r=0;r<9;r++)System.arraycopy(a[r],0,b[r],0,9);return b;}
}
