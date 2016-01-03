package com.tools.mynotes;

import com.tools.mynotes.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
 
/**
 * 2013-04-02  AlertDialog.class
 * @author Chris
 *
 */
public class CustomDialog extends Dialog {
 
    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }
 
    public CustomDialog(Context context) {
        super(context);
    }
 
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private int icon;
        private boolean cancelable;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;  
        private DialogInterface.OnClickListener 
                        positiveButtonClickListener,
                        negativeButtonClickListener;
 
        public Builder(Context context) {
            this.context = context;
        }
        
        /*	Dialog message	*/
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
        
        /*	Dialog title	*/
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        
        /*	Dialog icon	*/
        public Builder setIcon(int resId) {
        	this.icon = resId;
        	return this;
        }
        
        /*	Dialog cancel able	*/
        public Builder setCancelable(boolean cancelable) {
        	this.cancelable = cancelable;
            return this;
        }
        
        /*	Dialog content view	*/
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }
        
        /*	Positive button	*/
        public Builder setPositiveButton(int positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }
        public Builder setPositiveButton(String positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }
        
        /*	Negative button	*/
        public Builder setNegativeButton(int negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }
        public Builder setNegativeButton(String negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
        
        /**
         * Create the custom dialog
         */
        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context, R.style.MyDialog);
            View layout = inflater.inflate(R.layout.custom_dialog_relative, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            // set the negative button
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(
                                    		dialog, 
                                            DialogInterface.BUTTON_NEGATIVE);
                                    dialog.dismiss();
                                }
                            });
                } else {
                	dialog.dismiss();
                }
            } else {
                layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
            }
            // set the positive button
            if (positiveButtonText != null) {
            	((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.positiveButton))
                    	.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(
                                    		dialog, 
                                            DialogInterface.BUTTON_POSITIVE);
                                    dialog.dismiss();
                                }
                    });
                }
            } else {
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }
            // set the dialog title
            if (title != null) {
            	((TextView) layout.findViewById(R.id.title)).setText(title);
            }
            // set the dialog icon
            if (icon != 0) {
            	//TODO
//            	((ImageView) layout.findViewById(R.id.icon)).setBackgroundColor(icon);
            } else {
            	((ImageView) layout.findViewById(R.id.icon)).setVisibility(View.GONE);
            }
            //set cancel able
            dialog.setCancelable(cancelable);
            if (cancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set, add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content))
                        .addView(contentView, new LayoutParams(
                        				LayoutParams.WRAP_CONTENT, 
                                        LayoutParams.WRAP_CONTENT));
            }
            dialog.setContentView(layout);
            return dialog;
        }
        
        /**
         * show dialog
         * @return
         */
        public CustomDialog show() {
        	CustomDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}
