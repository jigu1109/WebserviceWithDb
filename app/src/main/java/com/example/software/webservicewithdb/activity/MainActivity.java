package com.example.software.webservicewithdb.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.software.webservicewithdb.APIInterface;
import com.example.software.webservicewithdb.R;
import com.example.software.webservicewithdb.adapter.MovieAdapetr;
import com.example.software.webservicewithdb.database.DatabaseHelper;
import com.example.software.webservicewithdb.model.Movie;
import com.example.software.webservicewithdb.model.MovieResponse;
import com.example.software.webservicewithdb.network.APIClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String API_KEY = "faae278f3f6bbbe9eea926527fcc5fff";
    private DatabaseHelper db;
    private List<Movie> notesList = new ArrayList<>();
    private MovieAdapetr movieAdapetr;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        db = new DatabaseHelper(this);

        recyclerView = (RecyclerView) findViewById(R.id.movies_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        movieAdapetr = new MovieAdapetr(notesList, R.layout.list_item_movie, getApplicationContext());

        if (API_KEY.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please obtain your API KEY first from themoviedb.org", Toast.LENGTH_LONG).show();
            return;
        }

        APIInterface apiInterface = APIClient.getRetrofit().create(APIInterface.class);
        Call<MovieResponse> call = apiInterface.getTopRatedMovie(API_KEY);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                List<Movie> movies = response.body().getResults();
                int[] id = new int[movies.size()];
                String[] title = new String[movies.size()];
                String[] overview = new String[movies.size()];
                String[] release_date = new String[movies.size()];
                double[] vote_avg = new double[movies.size()];
                Log.d("Tag", String.valueOf(response.body().getResults().size()));

                for (int i =0; i< movies.size(); i++){

                    id[i] = movies.get(i).getId();
                    title[i] = movies.get(i).getTitle();
                    overview[i] = movies.get(i).getOverview();
                    release_date[i] = movies.get(i).getReleaseDate();
                    vote_avg[i]=movies.get(i).getVoteAverage();

                    db.insertNote(id[i],release_date[i], title[i], overview[i], vote_avg[i]);
                    notesList.addAll(db.getAllNotes());
                }


                recyclerView.setAdapter(movieAdapetr);
                Log.d(TAG, "Number of movies received: " + movies.size());
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e(TAG, t.toString());
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            public static final float ALPHA_FULL = 1.0f;

            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {

                    //color : left side (swiping towards right)
                        p.setARGB(255, 255, 0, 0);
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);

                        // icon : left side (swiping towards right)
                        icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.baseline_delete_white_18dp);
                        c.drawBitmap(icon,
                                (float) itemView.getLeft() + convertDpToPx(16),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                p);
                    } else {

                        //color : right side (swiping towards left)
                        p.setARGB(255, 0, 255, 0);

                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);

                        //icon : left side (swiping towards right)
                        icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.sharp_thumb_up_white_48dp);
                        c.drawBitmap(icon,
                                (float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                p);
                    }

                    // Fade out the view when it is swiped out of the parent
                    final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);

                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            private int convertDpToPx(int dp){
                return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
            }
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //swiped position

                if (direction == ItemTouchHelper.LEFT) { //swipe left

                    notesList.remove(position);
                    movieAdapetr.notifyItemRemoved(position);

                    Toast.makeText(getApplicationContext(),"Swipped to left",Toast.LENGTH_SHORT).show();

                }else if(direction == ItemTouchHelper.RIGHT){//swipe right

                    db.deleteNote(notesList.get(position));
                    notesList.remove(position);
                    movieAdapetr.notifyItemRemoved(position);
                    Toast.makeText(getApplicationContext(),"Swipped to right and Delete Movie from list"  ,Toast.LENGTH_SHORT).show();


                }

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


}
