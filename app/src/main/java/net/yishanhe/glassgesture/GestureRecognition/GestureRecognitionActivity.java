package net.yishanhe.glassgesture.GestureRecognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import java.util.ArrayList;
import java.util.List;

/**
 * @author syi
 * This gesture will use the FastDTW method
 * to recogni
 */
public class GestureRecognitionActivity extends Activity {

    private static final String TAG = "GestureRegActivity";

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
//                Log.d(TAG, "position " + position + ",id " + id);
                switch (position){
                    case 0:
                        // @TODO run test
                        startActivity(new Intent(context, MyDtwTestActivity.class));
                        return;
                    case 4:
                        finish();
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



    private void createCards() {

        mCards = new ArrayList<CardBuilder>();

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Run Test"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Add Template"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Naive Matching"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Advanced Matching"));

        mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU).setFootnote("Tap to select.").setText("Return"));


    }





}
