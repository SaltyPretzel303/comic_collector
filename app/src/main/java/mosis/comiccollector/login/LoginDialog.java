package mosis.comiccollector.login;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import mosis.comiccollector.R;
import mosis.comiccollector.manager.AppManager;

public class LoginDialog extends Dialog {

    private Context app_context;

    private boolean forced;

    private TextView login_status_tv;

    private EditText username_et;
    private EditText password_et;

    private TextView rep_password_tw;
    private EditText rep_password_et;

    private Button pos_button;
    private Button neg_button;

    private Button context_switch;

    private View.OnClickListener login_action;
    private View.OnClickListener register_action;
    private View.OnClickListener to_login;
    private View.OnClickListener to_register;

    private BackPressHandler back_press_handler;

    public LoginDialog(Context context, boolean forced, BackPressHandler back_handler) {
        super(context);
        this.setContentView(R.layout.login_layout);

        this.forced = forced;

        if (this.forced) {
            this.setCanceledOnTouchOutside(false);
        } else {
            this.setCanceledOnTouchOutside(true);
        }

        this.app_context = context;

        this.back_press_handler = back_handler;

        this.initClickActions();

        this.initView();

    }

    private void initClickActions() {

        this.login_action = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO check inputs (empty strings, null, ... )

                final String username = username_et.getText().toString();
                final String password = password_et.getText().toString();


                AppManager.getInstance().getUsersManager().login(username, password, new OnResponseAction() {
                    @Override
                    public void execute(LoginResponseType response) {

                        if (response == LoginResponseType.Success) {

                            // write user to local storage

                            dismiss();
                            Toast.makeText(app_context, "Successfull login", Toast.LENGTH_SHORT).show();

                        } else {

                            handleError(response);

                            Toast.makeText(app_context, "Login error ... ", Toast.LENGTH_SHORT).show();

                        }

                    }
                });


            }
        };

        this.register_action = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = username_et.getText().toString();
                final String password = password_et.getText().toString();

                // TODO check inputs

                AppManager.getInstance().getUsersManager().register(username, password, new OnResponseAction() {
                    @Override
                    public void execute(LoginResponseType response) {

                        if (response == LoginResponseType.Success) {

                            showMessage("Successful registration ... ");
                            context_switch.performClick();
                            // switch form to loginForm


                        } else {

                            handleError(response);

                            Toast.makeText(app_context, "Registration error ... ", Toast.LENGTH_SHORT).show();

                        }


                    }
                });


            }
        };

        this.to_login = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO adjust text watchers
                // clear text inputs

                rep_password_et.setVisibility(View.GONE);
                rep_password_tw.setVisibility(View.GONE);

                pos_button.setOnClickListener(login_action);
                pos_button.setText("Login");


                context_switch.setText("Register");
                context_switch.setOnClickListener(to_register);

            }
        };

        this.to_register = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO adjust text watchers
                // clear text inputs

                rep_password_et.setVisibility(View.VISIBLE);
                rep_password_tw.setVisibility(View.VISIBLE);

                pos_button.setOnClickListener(register_action);
                pos_button.setText("Register");

                context_switch.setText("Login");
                context_switch.setOnClickListener(to_login);

            }
        };

    }

    private void initView() {

        this.login_status_tv = (TextView) this.findViewById(R.id.login_status_field);

        this.username_et = (EditText) this.findViewById(R.id.login_username_et);
        this.password_et = (EditText) this.findViewById(R.id.login_password_et);

        this.rep_password_tw = (TextView) this.findViewById(R.id.login_rep_password_tw);
        this.rep_password_tw.setVisibility(View.GONE);
        this.rep_password_et = (EditText) this.findViewById(R.id.login_rep_password_et);
        this.rep_password_et.setVisibility(View.GONE);

        this.pos_button = (Button) this.findViewById(R.id.login_pos_btn);
        this.neg_button = (Button) this.findViewById(R.id.login_neg_btn);

        this.context_switch = (Button) this.findViewById(R.id.login_context_switch);

        if (this.forced) {
            neg_button.setEnabled(false);
        } else {
            neg_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // close dialog
                    dismiss();
                }
            });
        }

        this.pos_button.setOnClickListener(this.login_action);
        this.pos_button.setText("Login");

        this.context_switch.setOnClickListener(this.to_register);
        this.context_switch.setText("Register");

    }

    @Override
    public void onBackPressed() {

        if (this.back_press_handler != null) {
            this.back_press_handler.execute();
        } else {
            super.onBackPressed();
        }

    }

    private void handleError(LoginResponseType error_type) {

        showMessage(error_type.toString());
        Log.e("FirebaseLogin", error_type.toString());

    }


    private void showMessage(String message) {

        this.login_status_tv.setText(message);

    }

}

