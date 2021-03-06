package space.nthompson.ruserpg.Activities.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import space.nthompson.ruserpg.R;

public class StrengthActivity extends AppCompatActivity {
    JSONObject jsonObject;
    JSONArray jsonArray;

    ArrayList<String> strengthList;
    ArrayList<Strength> strength;


    String strengthURL = "http://ruse-api.herokuapp.com/lookupstrength";
    String strengthPOSTURL = "http://ruse-api.herokuapp.com/postStrength";

    Button strengthBtn;
    EditText repTotal;

    String strengthID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strength);
        //set toolbar as the acting action bar
        Toolbar actionToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionToolbar);
        getSupportActionBar().setTitle("Add Strength");

        Intent intent = getIntent();
        final String workoutID = intent.getStringExtra("workout_id");
        final String twitterID = intent.getStringExtra("twitterID");
        String workoutDateTime = intent.getStringExtra("dateTime");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        new parseStengthAPI().execute();

        strengthBtn = (Button) findViewById(R.id.saveStrgthBtn);
        strengthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repTotal = (EditText) findViewById(R.id.rep_count);
                final String rep_total = repTotal.toString();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //code to do the HTTP request
                        OkHttpClient client = new OkHttpClient();
                        RequestBody formBody = new FormEncodingBuilder()
                                .add("reps", rep_total)
                                .add("fk_workout_id", workoutID)
                                .add("fk_workout_user_id", twitterID)
                                .add("fk_lookupstrength_id", strengthID)
                                .build();
                        System.out.println(strengthID);
                        Request request = new Request.Builder()
                                .url("http://ruse-api.herokuapp.com/postStrength")
                                .post(formBody)
                                .build();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            if (!response.isSuccessful())
                                throw new IOException("Unexpected code " + response);
                            //  System.out.println(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(StrengthActivity.this, WorkoutActivity.class);
                        startActivity(intent);
                    }

                });
                thread.start();

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exercise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class parseStengthAPI extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //locate Strength class
            strength = new ArrayList<Strength>();
            //create array to populate spinner with results
            strengthList = new ArrayList<String>();
            jsonArray = JSONfunctions.getJSONfromURL(strengthURL);
            try {
                for (int i = 0; i < jsonArray.length(); i++){
                    jsonObject = jsonArray.getJSONObject(i);

                    Strength str = new Strength();

                    str.setStrengthName(jsonObject.optString("strengthName"));
                    str.setYouTubeURL(jsonObject.optString("youTubeURL"));
                    strength.add(str);

                    strengthList.add(jsonObject.optString("strengthName"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void args){
            Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
            //spinner adapter
            mySpinner.setAdapter(new ArrayAdapter<String>(StrengthActivity.this, android.R.layout.simple_spinner_dropdown_item, strengthList));
            //spinner on item click listener
            mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView txtName = (TextView) findViewById(R.id.name);
                    TextView txtURL = (TextView) findViewById(R.id.youtubeURL);
                    txtName.setText(strength.get(i).getStrengthName());
                    txtURL.setText(
                            Html.fromHtml(
                                    "<a href=" + strength.get(i).getYouTubeURL() + ">Youtube URL</a> "));
                    strengthID = String.valueOf(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

}
