package gunn.brewski.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DashboardActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        /**
         * Creating all buttons instances
         * */
        // Dashboard Friends button
        final Button btn_categories = (Button) findViewById(R.id.btn_categories);

        // Dashboard Messages button
        final Button btn_beers = (Button) findViewById(R.id.btn_beers);

        // Dashboard Places button
        final Button btn_breweries = (Button) findViewById(R.id.btn_breweries);

        /**
         * Handling all button click events
         * */

        btn_categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingScreen(btn_categories.getText().toString().toLowerCase());
            }
        });

        btn_beers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingScreen(btn_beers.getText().toString().toLowerCase());
            }
        });

        btn_breweries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingScreen(btn_breweries.getText().toString().toLowerCase());
            }
        });
    }

    public void showLoadingScreen(String buttonText) {
        Intent loadingScreenIntent = new Intent(getApplicationContext(), LoadingScreenActivity.class);
        loadingScreenIntent.putExtra("screenLoading", buttonText);
        startActivity(loadingScreenIntent);
    }

    @Override
    public void onBackPressed() {
        return;
    }

}