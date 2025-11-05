package prog.android.centroalr.model;

public interface OnPasswordResetListener {
    void onResetSuccess();
    void onResetFailure(String error);
}