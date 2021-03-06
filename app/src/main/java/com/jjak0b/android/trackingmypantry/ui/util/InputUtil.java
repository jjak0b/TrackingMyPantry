package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Activity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import java.util.Objects;

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

    public static abstract class FieldTextWatcher implements TextWatcher {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        public abstract void afterTextChanged(Editable s);
    }

    public static void setText(@NonNull EditText view, @Nullable String text) {

        if( Objects.equals(view.getText().toString(), text) ) return;

        int selection = view.getSelectionEnd();
        int textLength = text == null ? 0 : text.length();

        view.setText(text);
        view.setSelection(Math.min(selection, textLength));
    }

    public static void setQuery(@NonNull SearchView view, @Nullable String query, boolean submit) {

        if( Objects.equals(view.getQuery().toString(), query) ) return;

        view.setQuery(query, submit);
    }
}
