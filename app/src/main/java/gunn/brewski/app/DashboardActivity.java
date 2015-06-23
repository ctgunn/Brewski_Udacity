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
                Intent styleIntent = new Intent(DashboardActivity.this, StyleListActivity.class);
                startActivity(styleIntent);
            }
        });

        btn_beers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent beerIntent = new Intent(DashboardActivity.this, BeerListActivity.class);
                startActivity(beerIntent);
            }
        });

        btn_breweries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent breweryIntent = new Intent(DashboardActivity.this, BreweryListActivity.class);
                startActivity(breweryIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}