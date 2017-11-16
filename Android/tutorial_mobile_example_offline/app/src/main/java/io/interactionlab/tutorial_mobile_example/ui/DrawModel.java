package io.interactionlab.tutorial_mobile_example.ui;

/**
 * Created by Huy on 31/08/2017.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the draw view and was adapted from miyosuda's TensorFlow Android MNIST example
 * for TensorFlow r.0.10. See: https://github.com/miyosuda/TensorFlowAndroidMNIST
 */
public class DrawModel {

    public static class LineElem {
        public float x;
        public float y;

        private LineElem(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Line {
        private List<LineElem> elems = new ArrayList<>();

        private Line() {
        }

        private void addElem(LineElem elem) {
            elems.add(elem);
        }

        public int getElemSize() {
            return elems.size();
        }

        public LineElem getElem(int index) {
            return elems.get(index);
        }
    }

    private Line mCurrentLine;

    private int mWidth;  // pixel width = 28
    private int mHeight; // pixel height = 28

    private List<Line> mLines = new ArrayList<>();

    public DrawModel(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void startLine(float x, float y) {
        mCurrentLine = new Line();
        mCurrentLine.addElem(new LineElem(x, y));
        mLines.add(mCurrentLine);
    }

    public void endLine() {
        mCurrentLine = null;
    }

    public void addLineElem(float x, float y) {
        if (mCurrentLine != null) {
            mCurrentLine.addElem(new LineElem(x, y));
        }
    }

    public int getLineSize() {
        return mLines.size();
    }

    public Line getLine(int index) {
        return mLines.get(index);
    }

    public void clear() {
        mLines.clear();
    }
}
