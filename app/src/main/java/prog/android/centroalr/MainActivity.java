package prog.android.centroalr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewFlipper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ViewFlipper flipper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTE: Put layout_mega.xml into res/layout as activity_mega.xml OR inflate programmatically.
        int layoutId = getResources().getIdentifier("layout_mega", "layout", getPackageName());
        if (layoutId == 0) {
            // fallback if you rename file to activity_mega.xml
            layoutId = getResources().getIdentifier("activity_mega", "layout", getPackageName());
        }
        setContentView(layoutId);
        int flipperId = getResources().getIdentifier("mega_view_flipper", "id", getPackageName());
        flipper = findViewById(flipperId);
    }

    public void showScreen(int index) {
        if (flipper != null && index >= 0 && index < flipper.getChildCount()) {
            flipper.setDisplayedChild(index);
        }
    }

    public void next(View v) {
        if (flipper != null) flipper.showNext();
    }

    public void previous(View v) {
        if (flipper != null) flipper.showPrevious();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LogInActivity.class));
            finish();
        }
    }

}