package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Activity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class InputUtil {

    // ignore enter First space on edittext
    public static InputFilter filterWhitespaces() {
        return new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder();

                for (int i = start; i < end; i++) {
                    if (!Character.isWhitespace(source.charAt(i))) {
                        builder.append( source.charAt( i ) );
                    }
                }

                return builder;
            }
        };
    }

    public static void hideKeyboard(Activity activity) {
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
