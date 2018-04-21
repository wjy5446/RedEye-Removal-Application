package com.example.vision_jy.redeyereomove2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by vision_jy on 2015-11-11.
 */
public class RedeyeFunction_jy {
    Context mContext;
    int nW, nH;
    double dInitThresh = 0;
    int[][] pixels;
    int[][] ImageRed, ImageGreen, ImageBlue;
    int[][] ImageRedness;
    double[][] ImageRednessValue;
    Rect[] TempRect;

    Mat mat_colorImage;
    Mat mat_grayImage;

    int nLabelCnt;
    Rect[] RectBox;
    Rect[] RectRedEye;

    int RoIWidth, RoIHeight;

    int[][][] FaceArrayRed, FaceArrayGreen, FaceArrayBlue;
    Rect[] FaceInfo;
    int nFaceCnt = 0;

    Mat M_Redness_Test;

    /////////////////////////////////////////////////////////////
    public RedeyeFunction_jy() {
        RectRedEye = new Rect[2];
    }
    public RedeyeFunction_jy(Context context) {
        mContext = context;
        RectRedEye = new Rect[2];
    }
    void copyFace(RedeyeFunction_jy Image, int FaceNum){
        nW = Image.FaceInfo[FaceNum].width;
        nH = Image.FaceInfo[FaceNum].height;

        ImageRed = new int[nH][nW];
        ImageGreen = new int[nH][nW];
        ImageBlue = new int[nH][nW];
        ImageRedness = new int[nH][nW];
        ImageRednessValue = new double[nH][nW];

        ImageRed = Image.FaceArrayRed[FaceNum];
        ImageGreen = Image.FaceArrayGreen[FaceNum];
        ImageBlue = Image.FaceArrayBlue[FaceNum];
    }

    int Labeling(int[][] ImageGray, int[][] Label, int nW, int nH, int nConnNumThre, int nConnNumThre2, double dThreshold) {
        int x, y, num, left, top, k;
        int[] r, area;

        r = new int[nW * nH];
        area = new int[nW * nH];

        for (y = 0; y < nH; y++)
            for (x = 0; x < nW; x++)
                if (ImageGray[y][x] > dThreshold) Label[y][x] = 0;
                else Label[y][x] = -1;

        for (x = 0; x < nW; x++) {
            Label[0][x] = -1;
            Label[nH - 1][x] = -1;
        }
        for (y = 0; y < nH; y++) {
            Label[y][0] = -1;
            Label[y][nW - 1] = -1;
        }

        num = -1;
        for (y = 0; y < nH; y++) {
            for (x = 0; x < nW; x++) {
                if (y > 0 && x > 0) {
                    if (Label[y][x] >= 0) {
                        left = Label[y][x - 1];
                        top = Label[y - 1][x];
                        if (left == -1 && top != -1) {
                            Label[y][x] = r[top];
                        } else if (left != -1 && top == -1) {
                            Label[y][x] = r[left];
                        } else if (left == -1 && top == -1) {
                            num++;
                            if (num >= nW * nH) {
                                return 0;
                            }

                            r[num] = num;
                            Label[y][x] = r[num];
                        } else if (left != -1 && top != -1) {
                            if (r[left] == r[top]) {
                                Label[y][x] = r[left];
                            } else if (r[left] > r[top]) {
                                Label[y][x] = r[top];
                                r[left] = r[top];
                            } else {
                                Label[y][x] = r[left];
                                r[top] = r[left];
                            }
                        }
                    }
                }
            }
        }

        for (k = 0; k <= num; k++) {
            if (k != r[k]) r[k] = r[r[k]];
            area[k] = 0;
        }

        for (y = 0; y < nH; y++)
            for (x = 0; x < nW; x++) {
                if (Label[y][x] > -1) {
                    Label[y][x] = r[Label[y][x]];
                    area[Label[y][x]]++;
                }
            }

        int cnt = 0;
        for (k = 0; k <= num; k++) {
            if (area[k] > nConnNumThre && area[k] < nConnNumThre2) r[k] = cnt++;
            else r[k] = -1;
        }

        for (y = 0; y < nH; y++)
            for (x = 0; x < nW; x++) {
                if (Label[y][x] >= 0)
                    Label[y][x] = r[Label[y][x]];
            }

        return cnt;
    }
    void SetLabelBoundBox(int[][] Label, int nW, int nH, Rect[] pBoundBox, int LabelCnt) {
        int i;
        int x, y;
        int left = nW, right = 0, top = nH, bottom = 0;

        for (i = 0; i < LabelCnt; i++)
            pBoundBox[i] = new Rect(0, 0, nW, nH);

        for (i = 0; i < LabelCnt; i++) {
            for (y = 0; y < nH; y++)
                for (x = 0; x < nW; x++) {
                    if (Label[y][x] < 0) continue;

                    if (Label[y][x] == i) {
                        if (left > x) left = x;
                        if (top > y) top = y;
                        if (right < x) right = x;
                        if (bottom < y) bottom = y;
                    }
                }

            pBoundBox[i].x = left;
            pBoundBox[i].y = top;
            pBoundBox[i].width = right - left;
            pBoundBox[i].height = bottom - top;

            left = nW;
            right = 0;
            top = nH;
            bottom = 0;
        }
    }

    void MakeImage(Bitmap bmp) {
        Bitmap tempBmp = bmp;

        nW = tempBmp.getWidth();
        nH = tempBmp.getHeight();

        mat_colorImage = new Mat(nH, nW, CvType.CV_8UC4);
        mat_grayImage = new Mat(nH, nW, CvType.CV_8UC1);

        Utils.bitmapToMat(tempBmp, mat_colorImage);
        Imgproc.cvtColor(mat_colorImage, mat_grayImage, Imgproc.COLOR_RGB2GRAY);

        pixels = new int[nH][nW];
        ImageRed = new int[nH][nW];
        ImageGreen = new int[nH][nW];
        ImageBlue = new int[nH][nW];
        ImageRedness = new int[nH][nW];
        ImageRednessValue = new double[nH][nW];

        for (int y = 0; y < nH; y++)
            for (int x = 0; x < nW; x++) {
                pixels[y][x] = tempBmp.getPixel(x, y);
                ImageRed[y][x] = (pixels[y][x] & 0xff0000) / 0x10000;
                ImageGreen[y][x] = (pixels[y][x] & 0x00ff00) / 0x100;
                ImageBlue[y][x] = (pixels[y][x] & 0x00ff);
            }

        return;
    }
    Bitmap MakeBmp() {
        Bitmap newbmp;
        int[] newpixel;
        newpixel = new int[nH * nW];

        for (int y = 0; y < nH; y++)
            for (int x = 0; x < nW; x++) {
                newpixel[(y * nW) + x] = 0xff * 0x1000000 + ImageRed[y][x] * 0x10000 + ImageGreen[y][x] * 0x100 + ImageBlue[y][x];
            }

        newbmp = Bitmap.createBitmap(newpixel, nW, nH, Bitmap.Config.ARGB_8888);

        return newbmp;
    }

    void FindFace() {
        Bitmap TempBmp = Bitmap.createBitmap(nW, nH, Bitmap.Config.ARGB_8888);

        try {
            InputStream io = mContext.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = io.read(buffer)) != -1) {
                os.write(buffer, 0, byteRead);
            }
            io.close();
            os.close();

            CascadeClassifier face = new CascadeClassifier(mCascadeFile.getAbsolutePath());

            if (face.empty()) {
                Log.e("TAG", "Failed to load cascade classifier");
                face = null;
            } else
                Log.i("TAG", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();
            mCascadeFile.delete();

            MatOfRect faceDetections = new MatOfRect();

            face.detectMultiScale(mat_grayImage, faceDetections);

            nFaceCnt = faceDetections.toArray().length;

            FaceArrayRed = new int[nFaceCnt][][];
            FaceArrayGreen = new int[nFaceCnt][][];
            FaceArrayBlue = new int[nFaceCnt][][];
            FaceInfo = new Rect[nFaceCnt];

            List<Rect> LRect = faceDetections.toList();

            for (int i = 0; i < nFaceCnt; i++) {

                FaceInfo[i] = LRect.get(i);

                FaceArrayRed[i] = new int[FaceInfo[i].height][FaceInfo[i].width];
                FaceArrayGreen[i] = new int[FaceInfo[i].height][FaceInfo[i].width];
                FaceArrayBlue[i] = new int[FaceInfo[i].height][FaceInfo[i].width];

                        for (int yy = 0; yy < FaceInfo[i].height; yy++)
                            for (int xx = 0; xx < FaceInfo[i].width; xx++) {
                                FaceArrayRed[i][yy][xx] = ImageRed[FaceInfo[i].y + yy][FaceInfo[i].x + xx];
                                FaceArrayGreen[i][yy][xx] = ImageGreen[FaceInfo[i].y + yy][FaceInfo[i].x + xx];
                                FaceArrayBlue[i][yy][xx] = ImageBlue[FaceInfo[i].y + yy][FaceInfo[i].x + xx];
                    }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", "Failed to load cascade. Exception thrown: " + e);
        }

        return;
    }
    Bitmap CheckFace() {
        Bitmap TempBmp = Bitmap.createBitmap(nW, nH, Bitmap.Config.ARGB_8888);

        try {
            InputStream io = mContext.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = io.read(buffer)) != -1) {
                os.write(buffer, 0, byteRead);
            }
            io.close();
            os.close();

            CascadeClassifier face = new CascadeClassifier(mCascadeFile.getAbsolutePath());

            if (face.empty()) {
                Log.e("TAG", "Failed to load cascade classifier");
                face = null;
            } else
                Log.i("TAG", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();
            mCascadeFile.delete();

            MatOfRect faceDetections = new MatOfRect();

            face.detectMultiScale(mat_grayImage, faceDetections);

            nFaceCnt = faceDetections.toArray().length;

            FaceArrayRed = new int[nFaceCnt][][];
            FaceArrayGreen = new int[nFaceCnt][][];
            FaceArrayBlue = new int[nFaceCnt][][];
            FaceInfo = new Rect[nFaceCnt];

            List<Rect> LRect = faceDetections.toList();

            for (int i = 0; i < nFaceCnt; i++) {

                FaceInfo[i] = LRect.get(i);

                FaceArrayRed[i] = new int[FaceInfo[i].height][FaceInfo[i].width];
                FaceArrayGreen[i] = new int[FaceInfo[i].height][FaceInfo[i].width];
                FaceArrayBlue[i] = new int[FaceInfo[i].height][FaceInfo[i].width];

                for (int yy = 0; yy < FaceInfo[i].height; yy++)
                    for (int xx = 0; xx < FaceInfo[i].width; xx++) {
                        FaceArrayRed[i][yy][xx] = ImageRed[FaceInfo[i].y + yy][FaceInfo[i].x + xx];
                        FaceArrayGreen[i][yy][xx] = ImageGreen[FaceInfo[i].y + yy][FaceInfo[i].x + xx];
                        FaceArrayBlue[i][yy][xx] = ImageBlue[FaceInfo[i].y + yy][FaceInfo[i].x + xx];
                    }
            }

            //Face Test
            for (Rect rect :  faceDetections.toArray()) {
                Imgproc.rectangle( mat_colorImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255, 255), 2);
            }

            Utils.matToBitmap( mat_colorImage, TempBmp);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", "Failed to load cascade. Exception thrown: " + e);
        }

        return TempBmp;
    }


    void Redness(int nlocalThresh) {
        double dRed, dLum, dRL, dAverRL = 0;
        double MaxRL = 0, MinRL = 0;
        int[][] TempImage;
        int[][] ImageLabel;
        int[] RednessHisto;
        int[] nTempRed = new int[500];
        int nTempW = 0, nTempH = 0;
        int x, y, num, nNumGrad = 1, nSumRL = 0;
        int[] dX = {1, -1, 0, 0, -1, 1, -1, 1};
        int[] dY = {0, 0, 1, -1, 1, -1, -1, 1};

        nTempW =nW;
        nTempH =nH;

        ImageLabel = new int[nTempH][nTempW];
        TempImage = new int[nTempH][nTempW];

        for (int i = 0; i < 500; i++)
            nTempRed[i] = 0;

        for (y = 0; y < nTempH; y++)        //Redness 값 계산
            for (x = 0; x < nTempW; x++) {
                dRed = (ImageRed[y][x]) - (ImageGreen[y][x] + ImageBlue[y][x]) / 2;
                dLum = (0.25 * ImageRed[y][x]) + (0.6 * ImageGreen[y][x]) + (0.15 * ImageBlue[y][x]);
                dRL = (2 * dRed) - dLum;

                ImageRednessValue[y][x] = dRL;

                if (MinRL > dRL) MinRL = dRL;
                if (MaxRL < dRL) MaxRL = dRL;
            }

        //상위 10%에 Threshold설정.
        num = (int) (MaxRL - MinRL + 1);
        RednessHisto = new int[num];
        for (int i = 0; i < num; i++) RednessHisto[i] = 0;
        for (y = 0; y < nTempH; y++)
            for (x = 0; x < nTempW; x++) {
                RednessHisto[(int) (ImageRednessValue[y][x] - MinRL)]++;
            }

        for (int i = num - 1; i > 0; i--) {
            nSumRL += RednessHisto[i];

            if (nSumRL > (nTempH * nTempW) / 20) {
                dInitThresh = i + MinRL;
                break;
            }
        }

        //Thresh값에 따른 초기 영역 설정.
        for (y = 0; y < nTempH; y++)
            for (x = 0; x < nTempW; x++) {
                if (ImageRednessValue[y][x] > dInitThresh)
                    TempImage[y][x] = 255;
                else
                    TempImage[y][x] = 0;
            }


        ////잡다한 점 제거
        if (nTempH * nTempW > 2000)
            nLabelCnt = Labeling(TempImage, ImageLabel, nTempW, nTempH, 5, (nTempH * nTempW) / 8, (double) 1);

        if (nTempH * nTempW < 2000)
            nLabelCnt = Labeling(TempImage, ImageLabel, nTempW, nTempH, 0, (nTempH * nTempW), (double) 1);

        if (nlocalThresh > 0) {

            num = (int) dInitThresh - nlocalThresh;

            //국소영역 Redness영역 설정
            while (nNumGrad > 0) {
                nNumGrad = 0;
                for (y = 1; y < nTempH - 1; y++)
                    for (x = 1; x < nTempW - 1; x++) {
                        if (ImageLabel[y][x] >= 0) {
                            //주위에 0이 있는지 확인 (테두리 확인)
                            for (int i = 0; i < 8; i++) {
                                if (ImageLabel[y + dY[i]][x + dX[i]] < 0) {
                                    if (ImageRednessValue[y + dY[i]][x + dX[i]] > num) {
                                        ImageLabel[y + dY[i]][x + dX[i]] = ImageLabel[y][x];
                                        nNumGrad++;
                                    }
                                }
                            }
                        }
                    }
            }

            //	붙어있는지 확인
            for (y = 1; y < nTempH - 1; y++)
                for (x = 1; x < nTempW - 1; x++) {
                    if (ImageLabel[y][x] >= 0) {
                        for (int i = 0; i < 4; i++) {
                            if ((ImageLabel[y + dY[i]][x + dX[i]] >= 0) && (ImageLabel[y + dY[i]][x + dX[i]] != ImageLabel[y][x])) {
                                ImageLabel[y][x] = -1;
                                break;
                            }
                        }
                    }
                }

            for (y = 0; y < nTempH; y++)
                for (x = 0; x < nTempW; x++) {
                    if (ImageLabel[y][x] >= 0)
                        TempImage[y][x] = 255;
                    else
                        TempImage[y][x] = 0;
                }
        }
////////////////////////
        M_Redness_Test = new Mat (nH, nW, CvType.CV_8UC3);
        Bitmap Bmp_Redness_Test = Bitmap.createBitmap(nW, nH, Bitmap.Config.ARGB_8888);

        double[] TempColor = new double[3];

        for(int h=0; h<nH; h++)
            for(int w=0; w<nW; w++)
            {
                if(TempImage[h][w]>0) {
                    TempColor[0] = 255;
                    TempColor[1] = 255;
                    TempColor[2] = 255;
                    M_Redness_Test.put(h, w, TempColor);
                }
                else {
                    TempColor[0] = 0;
                    TempColor[1] = 0;
                    TempColor[2] = 0;
                    M_Redness_Test.put(h, w, TempColor);
                }

            }

        Utils.matToBitmap(M_Redness_Test,Bmp_Redness_Test);
///////////////

        ImageRedness = TempImage;
        nLabelCnt = Labeling(ImageRedness, ImageLabel, nTempW, nTempH, 20, (nTempH * nTempW) / 64, (double) 1); //  Find Body Object
        RectBox = new Rect[nLabelCnt];
        SetLabelBoundBox(ImageLabel, nTempW, nTempH, RectBox, nLabelCnt);

        //Labeling Test
        for (Rect rect : RectBox) {
            Imgproc.rectangle(M_Redness_Test, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0, 255), 1);
        }
        Bitmap Bmp_Labeling_Test = Bitmap.createBitmap(nW,nH,Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(M_Redness_Test, Bmp_Labeling_Test);


        return;
    }
    boolean DetectEye_single() {
        double dW, dH;
        int nTop, nLeft;
        int y, x, numRed = 0, num = 0;
        boolean bSuccess = false;

        for (int i = 0; i < nLabelCnt; i++) {
            dW = RectBox[i].width;
            dH = RectBox[i].height;
            nTop = RectBox[i].y;
            nLeft = RectBox[i].x;

            for (y = 0; y < dH; y++)
                for (x = 0; x < dW; x++) {
                    if (ImageRedness[nTop + y][nLeft + x] > dInitThresh)
                        numRed++;
                    num++;
                }

            // 위치를 통해서 알아보기
            if (nTop > nH / 3 * 2 || nTop < nH / 9 || nLeft > nW / 9 * 8 || nLeft < nW / 9) {
                RectBox[i] = new Rect();
            }

            // Redness이미지 면적 비율 확인
            if ((double) numRed / (double) num < 0.5)
                RectBox[i] = new Rect();

            // 가로 세로 비율 확인
            if (!(dW / dH > 0.5 && dW / dH < 2))
                RectBox[i] = new Rect();

            numRed = 0;
            num = 0;
        }

        //눈이 아닌 라벨 제거
        TempRect = new Rect[nLabelCnt];

        y = 0;
        for (x = 0; x < nLabelCnt; x++) {
            if (!RectBox[x].equals(new Rect())) {
                TempRect[y] = RectBox[x];
                y++;
            }
        }

        Log.i("TAG", "NumOf y" + y);
        Log.i("TAG", "NumOf nLabelCnt" + nLabelCnt);
        RectBox = new Rect[y];
        nLabelCnt = y;
        for (x = 0; x < nLabelCnt; x++) {
            RectBox[x] = TempRect[x];
        }

        for (int i = 0; i < nLabelCnt; i++) {
            Log.i("TAG", "RectOf " + i + " x : " + RectBox[i].x);
            Log.i("TAG", "RectOf " + i + " y : " + RectBox[i].y);
            Log.i("TAG", "RectOf " + i + " width : " + RectBox[i].width);
            Log.i("TAG", "RectOf " + i + " height : " + RectBox[i].height);
            Log.i("TAG", "-----------------------------");
        }

        //잘 나왔는지 확인하는 부분.
        if (nLabelCnt < 2) {
            bSuccess = false;
        } else
            bSuccess = true;
//Single Test
/*
        for (Rect rect : RectBox) {
            Imgproc.rectangle( M_Redness_Test, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0, 255), 1);
        }
        Bitmap Bmp_Single_Test = Bitmap.createBitmap(nW,nH,Bitmap.Config.ARGB_8888);

        Utils.matToBitmap( M_Redness_Test, Bmp_Single_Test);
*/
        return bSuccess;
    }
    boolean DetectEye_double() {
        int i, j, k = 0;
        int num = nLabelCnt;
        double nArea1, nArea2;
        int nDistanceX, nDistanceY;
        int max;
        Rect[][] TempRect;
        boolean bSuccess;

        TempRect = new Rect[nLabelCnt * nLabelCnt][];

        for (i = 0; i < nLabelCnt * nLabelCnt; i++) {
            TempRect[i] = new Rect[2];
        }

        int ja = 0;
        for (i = 0; i < num; i++)
            for (j = i; j < num; j++) {
                if (i != j) {
                    // 각 쌍의 넓이 거리 정보 얻어오기
                    nArea1 = RectBox[i].width * RectBox[i].height;
                    nArea2 = RectBox[j].width * RectBox[j].height;
                    nDistanceY = Math.abs(RectBox[i].y - RectBox[j].y);
                    nDistanceX = Math.abs(RectBox[i].x - RectBox[j].x);

                    ja++;

                    // 넓이 정보 비교하기
                    if (0.5 < nArea1 / nArea2 && 2 > nArea1 / nArea2) {
                        //서로 거리 정보 비교하기
                        if (nDistanceY < (RectBox[i].height) && nDistanceX < (RectBox[i].width * 10) && nDistanceX > (RectBox[i].width * 2)) {
                            TempRect[k][0] = RectBox[i];
                            TempRect[k][1] = RectBox[j];
                            k++;
                        }
                    }
                }
            }
        if (k == 0) {
            //
        }

        RectRedEye[0] = TempRect[0][0];
        RectRedEye[1] = TempRect[0][1];

        if (k > 1) {
            max = TempRect[0][0].y - (nH / 2);

            for (i = 1; i < k; i++) {
                if (TempRect[i][0].y - (nH / 2) < max) {
                    RectRedEye[0] = TempRect[i][0];
                    RectRedEye[1] = TempRect[i][1];
                }
            }
        }

        if (k == 0) {
            bSuccess = false;
        } else {
            bSuccess = true;

            for (i = 0; i < 2; i++) {
                Log.i("TAG", "RedEyeRectOf " + i + " x : " + RectRedEye[i].x);
                Log.i("TAG", "RedEyeRectOf " + i + " y : " + RectRedEye[i].y);
                Log.i("TAG", "RedEyeRectOf " + i + " width : " + RectRedEye[i].width);
                Log.i("TAG", "RedEyeRectOf " + i + " height : " + RectRedEye[i].height);
                Log.i("TAG", "-----------------------------");
            }
        }


//Double Test
/*
        for (Rect rect : RectRedEye) {
            Imgproc.rectangle(M_Redness_Test, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0, 255), 1);
        }
        Bitmap Bmp_Double_Test = Bitmap.createBitmap(nW,nH,Bitmap.Config.ARGB_8888);

        Utils.matToBitmap( M_Redness_Test, Bmp_Double_Test);
*/
        return bSuccess;
    }
    void CorrectRedEye() {
        int x, y;
        int nExtend = 4;
        double p;

        for (int i = 0; i < 2; i++) {
            RoIHeight = (RectRedEye[i].y + RectRedEye[i].height) - RectRedEye[i].y;
            RoIWidth = (RectRedEye[i].x + RectRedEye[i].width) - RectRedEye[i].x;

            Log.i("TAG", "CorrectRedEye " + i + " width : " + RoIWidth);
            Log.i("TAG", "CorrectRedEye " + i + " height : " + RoIHeight);
            Log.i("TAG", "-----------------------------");

            // 값 수정하기
            p = 0.9;

            for (y = -nExtend; y < RoIHeight +nExtend; y++)
                for (x = -nExtend; x < RoIWidth +nExtend; x++) {

                    if (ImageRednessValue[RectRedEye[i].y + y][RectRedEye[i].x + x] > 0) {
                        ImageRed[RectRedEye[i].y + y][RectRedEye[i].x + x] = (int) ((1 - p) * ImageRed[RectRedEye[i].y + y][RectRedEye[i].x + x]
                                + p * (ImageGreen[RectRedEye[i].y + y][RectRedEye[i].x + x] + ImageBlue[RectRedEye[i].y + y][RectRedEye[i].x + x]) / 2.0);
                        ImageGreen[RectRedEye[i].y + y][RectRedEye[i].x + x] += (1.0 - p) * ImageGreen[RectRedEye[i].y + y][RectRedEye[i].x + x];
                        ImageBlue[RectRedEye[i].y + y][RectRedEye[i].x + x] += (1.0 - p) * ImageBlue[RectRedEye[i].y + y][RectRedEye[i].x + x];
                    }
                }
        }
        return;
    }

    void CorrectFace(RedeyeFunction_jy[] faceArray, int FaceNum) {
        int x, y;

        for (int i = 0; i < FaceNum; i++) {
            for (y = 0; y < FaceInfo[i].height; y++)
                for (x = 0; x < FaceInfo[i].width; x++) {
                    ImageRed[FaceInfo[i].y + y][FaceInfo[i].x + x] = faceArray[i].ImageRed[y][x];
                    ImageGreen[FaceInfo[i].y + y][FaceInfo[i].x + x] = faceArray[i].ImageGreen[y][x];
                    ImageBlue[FaceInfo[i].y + y][FaceInfo[i].x + x] = faceArray[i].ImageBlue[y][x];
                }
        }

        return;
    }
}

