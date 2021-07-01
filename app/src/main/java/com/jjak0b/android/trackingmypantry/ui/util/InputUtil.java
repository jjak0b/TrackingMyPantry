package com.jjak0b.android.trackingmypantry.ui.util;

import android.text.InputFilter;
import android.text.Spanned;

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
}
