package net.yishanhe.glassgesture;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import net.yishanhe.glassgesture.GestureAuthentication.GestureAuthActivity;
import net.yishanhe.glassgesture.GestureLogger.GestureLoggerActivity;
import net.yishanhe.glassgesture.GestureRecognition.GestureRecognitionActivity;
import net.yishanhe.glassgesture.SensorLogger.SensorLoggerActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author syi
 */
public class WelcomeActivity extends Activity {

    private static final String TAG = "WelcomeActivity";

    private CardScrollView mCardScroller;

    private List<CardBuilder> mCards;

    private Context context;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        context = this;

        createCards();

        mCardScroller = new CardScrollView(this);

        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return mCards.size();
            }

            @Override
            public Object getItem(int position) {
                return mCards.get(position);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mCards.get(position).getView(convertView, parent);
            }

            @Override
            public int getPosition(Object item) {
                return mCards.indexOf(item);
            }

        });

        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.TAP);
                Log.d(TAG, "position "+position+",id "+id);
                switch (position){
                    case 0:
                        openOptionsMenu();
                        return;
                    case 1:
                        startActivity(new Intent(context, SensorLoggerActivity.class));
                        return;
                    case 2:
                        startActivity(new Intent(context, GestureLoggerActivity.class));
                        return;
                    case 4:
                        startActivity(new Intent(context, GestureRecognitionActivity.class));
                        return;
                    case 5:
                        startActivity(new Intent(context, GestureAuthActivity.class));
                        return;
                }
            }
        });
        setContentView(mCardScroller);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void createCards() {

        mCards = new ArrayList<CardBuilder>();

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Swap for different components.").setText("Welcome."));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Sensor Logger"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Gesture Logger"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Activity"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Gesture Recognition"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Glass Authentication"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Put All Together"));

    }





}
