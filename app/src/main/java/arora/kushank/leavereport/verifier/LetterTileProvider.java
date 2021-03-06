package arora.kushank.leavereport.verifier;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextPaint;

import arora.kushank.leavereport.R;

/**
 * Created by Kushank-Arora on 20-Apr-17.
 */
public class LetterTileProvider {

    /** The number of available tile colors (see R.array.letter_tile_colors) */
    private static final int NUM_OF_TILE_COLORS = 8;

    /** The {@link TextPaint} used to draw the letter onto the tile */
    private final TextPaint mPaint = new TextPaint();
    /** The bounds that enclose the letter */
    private final Rect mBounds = new Rect();
    /** The {@link Canvas} to draw on */
    private final Canvas mCanvas = new Canvas();
    /** The first char of the name being displayed */
    private final char[] mFirstChar = new char[1];

    /** The background colors of the tile */
    private final TypedArray mColors;
    /** The font size used to display the letter */
    private final int mTileLetterFontSize;
    private final Context context;

    /**
     * Constructor for <code>LetterTileProvider</code>
     *
     * @param context The {@link Context} to use
     */
    public LetterTileProvider(Context context) {
        final Resources res = context.getResources();

        this.context=context;

        mPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);

        mColors = res.obtainTypedArray(R.array.letter_tile_colors);
        mTileLetterFontSize = res.getDimensionPixelSize(R.dimen.tile_letter_font_size);
    }

    /**
     * @param displayName The name used to create the letter for the tile
     * @param key The key used to generate the background color for the tile
     * @param width The desired width of the tile
     * @param height The desired height of the tile
     * @return A {@link Bitmap} that contains a letter used in the English
     *         alphabet or digit, if there is no letter or digit available, a
     *         default image is shown instead
     */
    public RoundedBitmapDrawable getLetterTile(String displayName, String key, int width, int height) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final char firstChar = displayName.charAt(0);

        final Canvas c = mCanvas;
        c.setBitmap(bitmap);
        c.drawColor(pickColor(key));

        if (Character.isLetterOrDigit(firstChar)) {
            mFirstChar[0] = Character.toUpperCase(firstChar);
            mPaint.setTextSize(mTileLetterFontSize);
            mPaint.getTextBounds(mFirstChar, 0, 1, mBounds);
            c.drawText(mFirstChar, 0, 1, width / 2, height / 2
                    + (mBounds.bottom - mBounds.top) / 2, mPaint);
        } else {

            mFirstChar[0] = 'A';
            mPaint.setTextSize(mTileLetterFontSize);
            mPaint.getTextBounds(mFirstChar, 0, 1, mBounds);
            c.drawText(mFirstChar, 0, 1, width / 2, height / 2
                    + (mBounds.bottom - mBounds.top) / 2, mPaint);
        }

        RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundedBitmap.setCornerRadius(width / 2);

        return roundedBitmap;
    }

    /**
     * @param key The key used to generate the tile color
     * @return A new or previously chosen color for <code>key</code> used as the
     *         tile background color
     */
    private int pickColor(String key) {
        // String.hashCode() is not supposed to change across java versions, so
        // this should guarantee the same key always maps to the same color
        final int color = Math.abs(key.hashCode()) % NUM_OF_TILE_COLORS;
        try {
            return mColors.getColor(color, Color.BLACK);
        } finally {
            mColors.recycle();
        }
    }

}