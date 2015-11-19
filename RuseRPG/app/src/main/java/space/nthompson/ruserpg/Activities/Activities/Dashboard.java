package space.nthompson.ruserpg.Activities.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import space.nthompson.ruserpg.R;


public class Dashboard extends AppCompatActivity{

    Context context = this;
    private Bitmap bm;
    private ImageView profile_photo;
    private ImageButton workoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //set toolbar as the acting action bar
        Toolbar actionToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Twitter API call to get user data (name, email, photo)
        twitterAPI();

        //Launch workout screen from 'add' button
        workoutBtn = (ImageButton) findViewById(R.id.workoutBtn);
        workoutBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, Workout.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
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
        if(id == R.id.action_logout){
            logoutUser();
        }

        return super.onOptionsItemSelected(item);
    }

    public void twitterAPI(){
        Twitter.getApiClient().getAccountService().verifyCredentials(true, false, new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {
                User user = userResult.data; //retrieve Twitter user
                String name = userResult.data.name; //retrieve Twitter user name
                String email = userResult.data.email; //retrieve Twitter user email

                //Retrieve profile photo URL's of various sizes for use
               // String photoUrlNormalSize = userResult.data.profileImageUrl;
                String photoUrlBiggerSize = userResult.data.profileImageUrl.replace("_normal", "_bigger");
               // String photoUrlMiniSize = userResult.data.profileImageUrl.replace("_normal", "_mini");
                String photoUrlOriginalSize = userResult.data.profileImageUrl.replace("_normal", "");
                System.out.println(photoUrlBiggerSize);
                //Start background Async process to handle modifying the UI thread from main
                new BackgroundUIProcesses().execute(photoUrlBiggerSize);
            }

            @Override
            public void failure(TwitterException e) {
            }
        });
    }




    //Function to close app rather than move down stack when back button is pressed from this activity
    @Override
    public void onBackPressed(){
        finish();
    }

    //Menu function to handle user logout from Twitter, stopping session and returning to login
    public boolean logoutUser(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setTitle("Logout?")
                .setMessage("Are you sure you want to logout?")
                .setCancelable(true)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Twitter.getSessionManager().clearActiveSession();
                        Twitter.logOut();
                        Intent intent = new Intent(Dashboard.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        dialog.create().show();
        return true;
    }

    private class BackgroundUIProcesses extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {
            String src = params[0];
            try {
                Log.e("src", src);
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                Log.e("Bitmap", "returned");
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Exception", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap myBitmap){
            profile_photo = (ImageView) findViewById(R.id.imageView);
            profile_photo.setImageBitmap(myBitmap);


        }
    }

}