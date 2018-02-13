package rikka.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

/**
 * Created by Rikka0w0 on 2/13/2018.
 */

public class InputBox {
    public static void show(Context context, String message, String title, String defaultText, final IInputBoxHandler eventHandler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        // Set up the input
        final EditText input = new EditText(context);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultText);
        builder.setView(input);

        // Set up the buttons
        builder.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventHandler.onClose(null);
                dialog.cancel();
            }
        });

        builder.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventHandler.onClose(input.getText().toString());
            }
        });

        builder.show();
    }

    public static interface IInputBoxHandler {
        /**
         * @param text null if cancelled
         */
        public void onClose(String text);
    }
}
